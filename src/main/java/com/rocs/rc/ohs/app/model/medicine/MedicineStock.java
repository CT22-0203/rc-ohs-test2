package com.rocs.rc.ohs.app.model.medicine;

import com.rocs.rc.ohs.app.model.personnel.Personnel;

import java.time.LocalDateTime;

/**
 * Represents a medicine stock batch in the clinic.
 */
public class MedicineStock {

    /** The unique ID of the medicine stock. */
    private Long id;

    /** The medicine associated with the stock. */
    private Medicine medicine;

    /** The personnel who recorded the medicine stock. */
    private Personnel personnel;

    /** The batch number of the medicine. */
    private String batchNumber;

    /** The quantity of medicine received. */
    private int quantityReceived;

    /** The expiration date of the medicine. */
    private LocalDateTime expirationDate;

    /** The current status of the medicine stock. */
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
     * Retrieves the medicine associated with the stock.
     *
     * @return the medicine associated with the stock.
     */
    public Medicine getMedicine() {
        return medicine;
    }

    /**
     * Sets the medicine associated with the stock.
     *
     * @param newMedicine the medicine associated with the stock.
     */
    public void setMedicine(Medicine newMedicine) {
        this.medicine = newMedicine;
    }

    /**
     * Retrieves the personnel who recorded the medicine stock.
     *
     * @return the personnel who recorded the medicine stock.
     */
    public Personnel getPersonnel() {
        return personnel;
    }

    /**
     * Sets the personnel who recorded the medicine stock.
     *
     * @param newPersonnel the personnel who recorded the medicine stock.
     */
    public void setPersonnel(Personnel newPersonnel) {
        this.personnel = newPersonnel;
    }

    /**
     * Retrieves the batch number of the medicine.
     *
     * @return the batch number of the medicine.
     */
    public String getBatchNumber() {
        return batchNumber;
    }

    /**
     * Sets the batch number of the medicine.
     *
     * @param newBatchNumber the batch number of the medicine.
     */
    public void setBatchNumber(String newBatchNumber) {
        this.batchNumber = newBatchNumber;
    }

    /**
     * Retrieves the quantity of medicine received.
     *
     * @return the quantity of medicine received.
     */
    public int getQuantityReceived() {
        return quantityReceived;
    }

    /**
     * Sets the quantity of medicine received.
     *
     * @param newQuantityReceived the quantity of medicine received.
     */
    public void setQuantityReceived(int newQuantityReceived) {
        this.quantityReceived = newQuantityReceived;
    }

    /**
     * Retrieves the expiration date of the medicine.
     *
     * @return the expiration date of the medicine.
     */
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the expiration date of the medicine.
     *
     * @param newExpirationDate the expiration date of the medicine.
     */
    public void setExpirationDate(LocalDateTime newExpirationDate) {
        this.expirationDate = newExpirationDate;
    }

    /**
     * Retrieves the current status of the medicine stock.
     *
     * @return the current status of the medicine stock.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the current status of the medicine stock.
     *
     * @param newStatus the current status of the medicine stock.
     */
    public void setStatus(String newStatus) {
        this.status = newStatus;
    }
}
