package com.rocs.rc.ohs.data.dao.medicinestock;

import com.rocs.rc.ohs.app.model.medicine.MedicineStock;

import java.util.List;

/**
 * Handles data access operations for medicine stock inventory.
 */
public interface MedicineStockDao {

    /**
     * Gets all medicine stocks with their details.
     *
     * @return The list of medicine stocks, or an empty list if none exist.
     */
    List<MedicineStock> findAll();

    /**
     * Gets a medicine stock using its id.
     *
     * @param id The id of the medicine stock.
     * @return The medicine stock, or null if not found.
     */
    MedicineStock findById(String id);

    /**
     * Gets medicine stocks that reached the minimum stock level.
     *
     * @return The list of low stock medicine items.
     */
    List<MedicineStock> findLowStock();

    /**
     * Searches medicine stocks by name, ignoring uppercase and lowercase.
     * Returns all stocks if the keyword is empty or null.
     *
     * @param keyword The medicine name or part of it.
     * @return The matching medicine stocks, or an empty list if none match.
     */
    List<MedicineStock> search(String keyword);

    /**
     * Updates the quantity of a medicine stock.
     *
     * @param id The id of the medicine stock.
     * @param quantity The new quantity, which must not be negative.
     * @return true if the update was successful.
     * @throws IllegalArgumentException if the quantity is negative or lower
     *         than the quantity already dispensed.
     */
    boolean updateQuantity(String id, int quantity);
}
