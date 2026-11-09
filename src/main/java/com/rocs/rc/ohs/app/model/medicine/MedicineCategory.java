package com.rocs.rc.ohs.app.model.medicine;

/**
 * Represents a category of medicine in the clinic.
 */
public class MedicineCategory {

    /** The unique ID of the medicine category. */
    private String id;

    /** The name of the medicine category. */
    private String categoryName;

    /**
     * Retrieves the unique ID of the medicine category.
     *
     * @return the unique ID of the medicine category.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the medicine category.
     *
     * @param newId the unique ID of the medicine category.
     */
    public void setId(String newId) {
        this.id = newId;
    }

    /**
     * Retrieves the name of the medicine category.
     *
     * @return the name of the medicine category.
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Sets the name of the medicine category.
     *
     * @param newCategoryName the name of the medicine category.
     */
    public void setCategoryName(String newCategoryName) {
        this.categoryName = newCategoryName;
    }
}
