package com.rocs.rc.ohs.app.model.medicine;

/**
 * Represents a medicine available in the clinic.
 */
public class Medicine {

    /** The unique ID of the medicine. */
    private Long id;

    /** The name of the medicine. */
    private String medicineName;

    /** The category of the medicine. */
    private MedicineCategory category;

    /** The unit of the medicine. */
    private String unit;

    /**
     * Retrieves the unique ID of the medicine.
     *
     * @return the unique ID of the medicine.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique ID of the medicine.
     *
     * @param newId the unique ID of the medicine.
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Retrieves the name of the medicine.
     *
     * @return the name of the medicine.
     */
    public String getMedicineName() {
        return medicineName;
    }

    /**
     * Sets the name of the medicine.
     *
     * @param newMedicineName the name of the medicine.
     */
    public void setMedicineName(String newMedicineName) {
        this.medicineName = newMedicineName;
    }

    /**
     * Retrieves the category of the medicine.
     *
     * @return the category of the medicine.
     */
    public MedicineCategory getCategory() {
        return category;
    }

    /**
     * Sets the category of the medicine.
     *
     * @param newCategory the category of the medicine.
     */
    public void setCategory(MedicineCategory newCategory) {
        this.category = newCategory;
    }

    /**
     * Retrieves the unit of the medicine.
     *
     * @return the unit of the medicine.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the unit of the medicine.
     *
     * @param newUnit the unit of the medicine.
     */
    public void setUnit(String newUnit) {
        this.unit = newUnit;
    }
}
