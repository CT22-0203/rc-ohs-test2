package com.rocs.rc.ohs.data.dao.medicinestock.impl;

import com.rocs.rc.ohs.app.model.medicine.Medicine;
import com.rocs.rc.ohs.app.model.medicine.MedicineCategory;
import com.rocs.rc.ohs.app.model.medicine.MedicineStock;
import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import com.rocs.rc.ohs.data.dao.medicinestock.MedicineStockDao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles database operations for medicine stock records.
 */
public class MedicineStockDaoImpl implements MedicineStockDao {

    /** Logs database errors. */
    private static final Logger LOGGER =
            Logger.getLogger(MedicineStockDaoImpl.class.getName());

    /** Minimum remaining quantity before stock is considered low. */
    private static final int LOW_STOCK_LEVEL = 10;

    /** Status for stocks with low remaining quantity. */
    private static final String STATUS_LOW_STOCK = "Low Stock";

    /** Status for stocks with enough remaining quantity. */
    private static final String STATUS_ENOUGH = "Enough";

    /** Selects medicine stock records together with their medicine details. */
    private static final String SELECT_STOCK =
            "select ms.id, ms.batch_number, ms.quantity_received, "
                    + "ms.expiration_date, ms.status, "
                    + "m.id as medicine_id, m.medicine_name, m.unit, "
                    + "mc.id as category_id, mc.category_name "
                    + "from medicine_stock ms "
                    + "join medicine m on m.id = ms.medicine_id "
                    + "left join medicine_category mc "
                    + "on mc.id = m.category_id ";

    /**
     * Gets all medicine stocks from the database.
     *
     * @return All medicine stocks, or an empty list if none are found.
     */
    @Override
    public List<MedicineStock> findAll() {
        try (Connection conn = ConnectionHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     SELECT_STOCK + "order by ms.batch_number")) {
            return readStocks(stmt);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Unable to retrieve all medicine stocks.", e);
        }
        return new ArrayList<>();
    }

    /**
     * Finds a stock record using its ID.
     *
     * @param id The stock record ID.
     * @return The matching medicine stock, or null if not found.
     */
    @Override
    public MedicineStock findById(String id) {
        MedicineStock stock = null;
        try (Connection conn = ConnectionHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     SELECT_STOCK + "where ms.id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stock = mapStock(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Unable to retrieve medicine stock by id.", e);
        }
        return stock;
    }

    /**
     * Gets stocks that have reached the minimum quantity.
     *
     * @return A list of low stock medicine records.
     */
    @Override
    public List<MedicineStock> findLowStock() {
        try (Connection conn = ConnectionHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     SELECT_STOCK
                             + "where fn_remaining_stock(ms.id) <= ? "
                             + "order by ms.batch_number")) {
            stmt.setInt(1, LOW_STOCK_LEVEL);
            return readStocks(stmt);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Unable to retrieve low stock medicine stocks.", e);
        }
        return new ArrayList<>();
    }

    /**
     * Searches stocks using the medicine name.
     * The search is case-insensitive and accepts partial names.
     * Returns all stocks when the keyword is empty or null.
     *
     * @param keyword The name or keyword to search for.
     * @return The matching medicine stocks.
     */
    @Override
    public List<MedicineStock> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
        try (Connection conn = ConnectionHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     SELECT_STOCK
                             + "where lower(m.medicine_name) like ? "
                             + "order by ms.batch_number")) {
            stmt.setString(1, pattern);
            return readStocks(stmt);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Unable to search medicine stocks.", e);
        }
        return new ArrayList<>();
    }

    /**
     * Changes the received quantity and updates the stock status.
     *
     * @param id The stock record to update.
     * @param quantity The new quantity. Negative values are not allowed.
     * @return true if the record was updated successfully.
     * @throws IllegalArgumentException if the quantity is negative or lower
     *         than the quantity already dispensed.
     */
    @Override
    public boolean updateQuantity(String id, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Quantity must not be negative.");
        }
        try (Connection conn = ConnectionHelper.getConnection()) {
            int dispensed = getDispensedQuantity(conn, id);
            if (quantity < dispensed) {
                throw new IllegalArgumentException(
                        "Quantity must not be lower than the quantity "
                                + "already dispensed.");
            }
            String status = (quantity - dispensed) <= LOW_STOCK_LEVEL
                    ? STATUS_LOW_STOCK : STATUS_ENOUGH;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "update medicine_stock "
                            + "set quantity_received = ?, status = ? "
                            + "where id = ?")) {
                stmt.setInt(1, quantity);
                stmt.setString(2, status);
                stmt.setString(3, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Unable to update medicine stock quantity.", e);
        }
        return false;
    }

    /**
     * Reads the query results and converts each row into a stock object.
     *
     * @param stmt The prepared statement to execute.
     * @return The list of retrieved medicine stocks.
     * @throws SQLException if the query fails.
     */
    private List<MedicineStock> readStocks(PreparedStatement stmt)
            throws SQLException {
        List<MedicineStock> stocks = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                stocks.add(mapStock(rs));
            }
        }
        return stocks;
    }

    /**
     * Checks how many units have already been dispensed.
     *
     * @param conn The active database connection.
     * @param id The stock record to check.
     * @return The total dispensed quantity, or 0 if none.
     * @throws SQLException if the query fails.
     */
    private int getDispensedQuantity(Connection conn, String id)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "select coalesce(sum(quantity_to_dispense), 0) "
                        + "from prescription where medicine_stock_id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Creates a MedicineStock object from the current result row.
     *
     * @param rs The result set containing the stock details.
     * @return The mapped medicine stock.
     * @throws SQLException if a column cannot be read.
     */
    private MedicineStock mapStock(ResultSet rs) throws SQLException {
        MedicineStock stock = new MedicineStock();
        stock.setId(rs.getString("id"));
        stock.setMedicine(mapMedicine(rs));
        stock.setBatchNumber(rs.getString("batch_number"));
        stock.setQuantityReceived(rs.getInt("quantity_received"));
        Date expiration = rs.getDate("expiration_date");
        stock.setExpirationDate(expiration == null
                ? null : expiration.toLocalDate().atStartOfDay());
        stock.setStatus(rs.getString("status"));
        return stock;
    }

    /**
     * Gets the medicine details from the current result row.
     *
     * @param rs The result set containing the medicine information.
     * @return The mapped medicine, including its category if available.
     * @throws SQLException if a column cannot be read.
     */
    private Medicine mapMedicine(ResultSet rs) throws SQLException {
        Medicine medicine = new Medicine();
        medicine.setId(rs.getString("medicine_id"));
        medicine.setMedicineName(rs.getString("medicine_name"));
        medicine.setUnit(rs.getString("unit"));
        String categoryId = rs.getString("category_id");
        if (categoryId != null) {
            MedicineCategory category = new MedicineCategory();
            category.setId(categoryId);
            category.setCategoryName(rs.getString("category_name"));
            medicine.setCategory(category);
        }
        return medicine;
    }
}
