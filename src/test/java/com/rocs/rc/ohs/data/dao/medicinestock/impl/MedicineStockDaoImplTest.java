package com.rocs.rc.ohs.data.dao.medicinestock.impl;

import com.rocs.rc.ohs.app.model.medicine.Medicine;
import com.rocs.rc.ohs.app.model.medicine.MedicineStock;
import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.stubbing.OngoingStubbing;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the medicine stock DAO using the RCISD-5 test cases.
 */
class MedicineStockDaoImplTest {

    /** Index for Paracetamol 500mg. */
    private static final int PARACETAMOL = 0;

    /** Index for Amoxicillin 500mg. */
    private static final int AMOXICILLIN = 1;

    /** Seed stock IDs. */
    private static final String[] STOCK_IDS = {
            "cff69d39-caa4-4a79-978b-8927f5b0c1a4",
            "70aaf305-a7f5-41f1-bd0a-6ad73c042be9"
    };

    /** Seed medicine IDs. */
    private static final String[] MEDICINE_IDS = {
            "ebb3209c-50ea-4fbd-8b7e-be4540ab5b57",
            "36c00788-fcfd-485e-8ad3-de08fd49cb76"
    };

    /** Medicine names used in the tests. */
    private static final String[] MEDICINE_NAMES = {
            "Paracetamol 500mg", "Amoxicillin 500mg"
    };

    /** Units for each medicine. */
    private static final String[] UNITS = {"tablet", "capsule"};

    /** Medicine category IDs. */
    private static final String[] CATEGORY_IDS = {
            "fde2280e-3564-4ced-87d5-a06454eec0b3",
            "e855961e-acf2-4bec-9262-30d1961d40b1"
    };

    /** Medicine category names. */
    private static final String[] CATEGORY_NAMES = {"Analgesic", "Antibiotic"};

    /** Batch numbers for the seed stocks. */
    private static final String[] BATCH_NUMBERS = {"B-001", "B-002"};

    /** Expiration dates for the seed stocks. */
    private static final String[] EXPIRATION_DATES = {
            "2027-12-31", "2027-06-30"
    };

    /** Quantity received for each stock. */
    private static final int QUANTITY_RECEIVED = 50;

    /** Default stock status. */
    private static final String STATUS_ENOUGH = "Enough";

    /** Mock for the database connection helper. */
    private MockedStatic<ConnectionHelper> connectionHelper;

    /** Mock database connection. */
    private Connection connection;

    /** Mock for select queries. */
    private PreparedStatement statement;

    /** Mock result set for stock records. */
    private ResultSet resultSet;

    /** Mock for the dispensed quantity query. */
    private PreparedStatement dispensedStatement;

    /** Mock result set for dispensed quantities. */
    private ResultSet dispensedResultSet;

    /** Mock for update queries. */
    private PreparedStatement updateStatement;

    /** DAO instance used in the tests. */
    private MedicineStockDaoImpl dao;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        dispensedStatement = mock(PreparedStatement.class);
        dispensedResultSet = mock(ResultSet.class);
        updateStatement = mock(PreparedStatement.class);

        connectionHelper = mockStatic(ConnectionHelper.class);
        connectionHelper.when(ConnectionHelper::getConnection)
                .thenReturn(connection);

        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(connection.prepareStatement(startsWith("select coalesce")))
                .thenReturn(dispensedStatement);
        when(connection.prepareStatement(startsWith("update")))
                .thenReturn(updateStatement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(dispensedStatement.executeQuery()).thenReturn(dispensedResultSet);
        when(updateStatement.executeUpdate()).thenReturn(1);

        dao = new MedicineStockDaoImpl();
    }

    @AfterEach
    void tearDown() {
        connectionHelper.close();
    }

    @Test
    @DisplayName("RCISD-5-TC1, TC2, TC4: all medicine stocks with details")
    void findAllReturnsEveryMedicineStockWithItsMedicineDetails()
            throws SQLException {
        stubRows(PARACETAMOL, AMOXICILLIN);

        List<MedicineStock> stocks = dao.findAll();

        assertEquals(2, stocks.size());
        assertStock(stocks.get(0), PARACETAMOL);
        assertStock(stocks.get(1), AMOXICILLIN);
    }

    @Test
    @DisplayName("RCISD-5-TC7: no medicine records")
    void findAllReturnsEmptyListWhenNoMedicineRecordsExistOrDatabaseFails()
            throws SQLException {
        stubRows();

        assertTrue(dao.findAll().isEmpty());

        when(connection.prepareStatement(anyString()))
                .thenThrow(new SQLException("connection failed"));

        List<MedicineStock> onFailure = dao.findAll();

        assertNotNull(onFailure);
        assertTrue(onFailure.isEmpty());
    }

    @Test
    @DisplayName("RCISD-5-TC3: details of the selected medicine stock")
    void findByIdReturnsStockWithMedicineDetailsForTheGivenId()
            throws SQLException {
        stubRows(PARACETAMOL);

        MedicineStock stock = dao.findById(STOCK_IDS[PARACETAMOL]);

        assertNotNull(stock);
        assertStock(stock, PARACETAMOL);
        verify(statement).setString(1, STOCK_IDS[PARACETAMOL]);
    }

    @Test
    void findByIdLeavesExpirationDateAndCategoryEmptyWhenNotRecorded()
            throws SQLException {
        stubRows(PARACETAMOL);
        when(resultSet.getDate("expiration_date")).thenReturn((Date) null);
        when(resultSet.getString("category_id")).thenReturn((String) null);

        MedicineStock stock = dao.findById(STOCK_IDS[PARACETAMOL]);

        assertNotNull(stock);
        assertNull(stock.getExpirationDate());
        assertNotNull(stock.getMedicine());
        assertNull(stock.getMedicine().getCategory());
    }

    @Test
    void findByIdReturnsNullWhenStockIsNotFoundOrDatabaseFails()
            throws SQLException {
        stubRows();

        assertNull(dao.findById("missing"));
        verify(statement).setString(1, "missing");

        when(statement.executeQuery()).thenThrow(new SQLException("failed"));

        assertNull(dao.findById(STOCK_IDS[PARACETAMOL]));
    }

    @Test
    @DisplayName("RCISD-5-TC5: search using the full medicine name")
    void searchReturnsTheMatchingStockWhenTheFullMedicineNameIsGiven()
            throws SQLException {
        stubRows(AMOXICILLIN);

        List<MedicineStock> stocks = dao.search("Amoxicillin 500mg");

        assertEquals(1, stocks.size());
        assertStock(stocks.get(0), AMOXICILLIN);
        verify(statement).setString(1, "%amoxicillin 500mg%");
    }

    @Test
    @DisplayName("RCISD-5-TC6: search using a keyword")
    void searchReturnsTheMatchingStockWhenAKeywordIsGivenIgnoringCase()
            throws SQLException {
        stubRows(PARACETAMOL);

        List<MedicineStock> stocks = dao.search("Paracetamol");

        assertEquals(1, stocks.size());
        assertStock(stocks.get(0), PARACETAMOL);

        stubRows(PARACETAMOL);

        assertEquals(1, dao.search("  PARACETAMOL ").size());
        verify(statement, times(2)).setString(1, "%paracetamol%");
    }

    @Test
    void searchReturnsAllStocksWhenTheKeywordIsBlank() throws SQLException {
        stubRows(PARACETAMOL, AMOXICILLIN);

        assertEquals(2, dao.search("   ").size());

        stubRows(PARACETAMOL, AMOXICILLIN);

        assertEquals(2, dao.search(null).size());
        verify(statement, never()).setString(anyInt(), anyString());
    }

    @Test
    void searchReturnsEmptyListWhenNothingMatchesOrDatabaseFails()
            throws SQLException {
        stubRows();

        assertTrue(dao.search("Ibuprofen").isEmpty());

        when(statement.executeQuery()).thenThrow(new SQLException("failed"));

        List<MedicineStock> onFailure = dao.search("Ibuprofen");

        assertNotNull(onFailure);
        assertTrue(onFailure.isEmpty());
    }

    @Test
    void findLowStockReturnsStocksUsingTheLowStockLevel() throws SQLException {
        stubRows(AMOXICILLIN);

        List<MedicineStock> stocks = dao.findLowStock();

        assertEquals(1, stocks.size());
        assertStock(stocks.get(0), AMOXICILLIN);
        verify(statement).setInt(1, 10);
    }

    @Test
    void findLowStockReturnsEmptyListWhenNoStockIsLowOrDatabaseFails()
            throws SQLException {
        stubRows();

        assertTrue(dao.findLowStock().isEmpty());

        when(statement.executeQuery()).thenThrow(new SQLException("failed"));

        List<MedicineStock> onFailure = dao.findLowStock();

        assertNotNull(onFailure);
        assertTrue(onFailure.isEmpty());
    }

    @Test
    void updateQuantityRejectsInvalidQuantitiesButAcceptsEqualToDispensed()
            throws SQLException {
        String id = STOCK_IDS[PARACETAMOL];

        assertThrows(IllegalArgumentException.class,
                () -> dao.updateQuantity(id, -1));
        connectionHelper.verify(ConnectionHelper::getConnection, never());

        stubDispensed(20);

        assertThrows(IllegalArgumentException.class,
                () -> dao.updateQuantity(id, 19));
        verify(connection, never()).prepareStatement(startsWith("update"));

        assertTrue(dao.updateQuantity(id, 20));
        verify(updateStatement).setInt(1, 20);
    }

    @Test
    void updateQuantitySavesQuantityAndDerivesStatusFromRemainingStock()
            throws SQLException {
        String id = STOCK_IDS[PARACETAMOL];
        stubDispensed(5);

        assertTrue(dao.updateQuantity(id, 15));
        verify(updateStatement).setInt(1, 15);
        verify(updateStatement).setString(2, "Low Stock");

        assertTrue(dao.updateQuantity(id, 16));
        verify(updateStatement).setInt(1, 16);
        verify(updateStatement).setString(2, STATUS_ENOUGH);
        verify(updateStatement, times(2)).setString(3, id);
    }

    @Test
    void updateQuantityTreatsMissingDispensedRowAsNothingDispensed()
            throws SQLException {
        when(dispensedResultSet.next()).thenReturn(false);

        assertTrue(dao.updateQuantity(STOCK_IDS[PARACETAMOL], 50));

        verify(updateStatement).setInt(1, 50);
        verify(updateStatement).setString(2, STATUS_ENOUGH);
    }

    @Test
    void updateQuantityReturnsFalseWhenNoRowIsUpdatedOrDatabaseFails()
            throws SQLException {
        stubDispensed(0);
        when(updateStatement.executeUpdate()).thenReturn(0);

        assertFalse(dao.updateQuantity("missing", 50));

        when(dispensedStatement.executeQuery())
                .thenThrow(new SQLException("failed"));

        assertFalse(dao.updateQuantity(STOCK_IDS[PARACETAMOL], 50));
    }

    /**
     * Checks the stock details against the seed data.
     *
     * @param stock The stock record to check.
     * @param row The seed data index.
     */
    private void assertStock(MedicineStock stock, int row) {
        assertEquals(STOCK_IDS[row], stock.getId());
        assertEquals(BATCH_NUMBERS[row], stock.getBatchNumber());
        assertEquals(QUANTITY_RECEIVED, stock.getQuantityReceived());
        assertEquals(LocalDate.parse(EXPIRATION_DATES[row]).atStartOfDay(),
                stock.getExpirationDate());
        assertEquals(STATUS_ENOUGH, stock.getStatus());
        Medicine medicine = stock.getMedicine();
        assertNotNull(medicine);
        assertEquals(MEDICINE_IDS[row], medicine.getId());
        assertEquals(MEDICINE_NAMES[row], medicine.getMedicineName());
        assertEquals(UNITS[row], medicine.getUnit());
        assertNotNull(medicine.getCategory());
        assertEquals(CATEGORY_IDS[row], medicine.getCategory().getId());
        assertEquals(CATEGORY_NAMES[row],
                medicine.getCategory().getCategoryName());
    }

    /**
     * Prepares the result set using the selected seed rows.
     *
     * @param rows The seed indexes to include, or none for an empty result.
     * @throws SQLException if the result set setup fails.
     */
    private void stubRows(int... rows) throws SQLException {
        OngoingStubbing<Boolean> hasNext = when(resultSet.next());
        for (int i = 0; i < rows.length; i++) {
            hasNext = hasNext.thenReturn(true);
        }
        hasNext.thenReturn(false);

        stubColumn("id", STOCK_IDS, rows);
        stubColumn("batch_number", BATCH_NUMBERS, rows);
        stubColumn("medicine_id", MEDICINE_IDS, rows);
        stubColumn("medicine_name", MEDICINE_NAMES, rows);
        stubColumn("unit", UNITS, rows);
        stubColumn("category_id", CATEGORY_IDS, rows);
        stubColumn("category_name", CATEGORY_NAMES, rows);

        if (rows.length > 0) {
            OngoingStubbing<Date> expiration =
                    when(resultSet.getDate("expiration_date"));
            for (int row : rows) {
                expiration = expiration.thenReturn(
                        Date.valueOf(EXPIRATION_DATES[row]));
            }
        }
        when(resultSet.getInt("quantity_received"))
                .thenReturn(QUANTITY_RECEIVED);
        when(resultSet.getString("status")).thenReturn(STATUS_ENOUGH);
    }

    /**
     * Sets up a result set column using the selected seed values.
     *
     * @param column The column name.
     * @param values The values to use.
     * @param rows The seed indexes to include.
     * @throws SQLException if the column setup fails.
     */
    private void stubColumn(String column, String[] values, int... rows)
            throws SQLException {
        if (rows.length == 0) {
            return;
        }
        OngoingStubbing<String> stub = when(resultSet.getString(column));
        for (int row : rows) {
            stub = stub.thenReturn(values[row]);
        }
    }

    /**
     * Sets up the mocked dispensed quantity.
     *
     * @param dispensed The quantity already dispensed.
     * @throws SQLException if the result set setup fails.
     */
    private void stubDispensed(int dispensed) throws SQLException {
        when(dispensedResultSet.next()).thenReturn(true);
        when(dispensedResultSet.getInt(1)).thenReturn(dispensed);
    }
}
