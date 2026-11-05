package com.rocs.rc.ohs.app.model.medicine;

import java.time.LocalDateTime;

/**
 * Represents a stock of a medicine in the clinic.
 */
public class MedicineStock {

    /** The unique ID of the medicine stock. */
    private Long id;

    /** The ID of the medicine. */
    private Long medicineId;

    /** The ID of the personnel who received it. */
    private Long personnelId;

    /** The batch number of the stock. */
    private String batchNumber;

    /** The quantity received in the stock. */
    private int quantityReceived;

    /** The expiration date of the stock. */
    private LocalDateTime expirationDate;

    /** The status of the stock. */
    private String status;

    /**
     * Retrieves the unique ID of the medicine stock.
     *
     * @return the unique ID of the medicine stock.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique ID of the medicine stock.
     *
     * @param newId the unique ID of the medicine stock.
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Retrieves the ID of the medicine.
     *
     * @return the ID of the medicine.
     */
    public Long getMedicineId() {
        return medicineId;
    }

    /**
     * Sets the ID of the medicine.
     *
     * @param newMedicineId the ID of the medicine.
     */
    public void setMedicineId(Long newMedicineId) {
        this.medicineId = newMedicineId;
    }

    /**
     * Retrieves the ID of the personnel who received it.
     *
     * @return the ID of the personnel who received it.
     */
    public Long getPersonnelId() {
        return personnelId;
    }

    /**
     * Sets the ID of the personnel who received it.
     *
     * @param newPersonnelId the ID of the personnel who received it.
     */
    public void setPersonnelId(Long newPersonnelId) {
        this.personnelId = newPersonnelId;
    }

    /**
     * Retrieves the batch number of the stock.
     *
     * @return the batch number of the stock.
     */
    public String getBatchNumber() {
        return batchNumber;
    }

    /**
     * Sets the batch number of the stock.
     *
     * @param newBatchNumber the batch number of the stock.
     */
    public void setBatchNumber(String newBatchNumber) {
        this.batchNumber = newBatchNumber;
    }

    /**
     * Retrieves the quantity received in the stock.
     *
     * @return the quantity received in the stock.
     */
    public int getQuantityReceived() {
        return quantityReceived;
    }

    /**
     * Sets the quantity received in the stock.
     *
     * @param newQuantityReceived the quantity received in the stock.
     */
    public void setQuantityReceived(int newQuantityReceived) {
        this.quantityReceived = newQuantityReceived;
    }

    /**
     * Retrieves the expiration date of the stock.
     *
     * @return the expiration date of the stock.
     */
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the expiration date of the stock.
     *
     * @param newExpirationDate the expiration date of the stock.
     */
    public void setExpirationDate(LocalDateTime newExpirationDate) {
        this.expirationDate = newExpirationDate;
    }

    /**
     * Retrieves the status of the stock.
     *
     * @return the status of the stock.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the stock.
     *
     * @param newStatus the status of the stock.
     */
    public void setStatus(String newStatus) {
        this.status = newStatus;
    }
}
