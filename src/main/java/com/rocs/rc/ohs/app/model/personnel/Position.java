package com.rocs.rc.ohs.app.model.personnel;

import com.rocs.rc.ohs.app.model.department.Department;

/**
 * Represents a personnel position in the system.
 */
public class Position {

    /** The unique ID of the position. */
    private String id;

    /** The name of the position. */
    private String positionName;

    /** The category of the position. */
    private String category;

    /** The department associated with the position. */
    private Department department;

    /**
     * Retrieves the unique ID assigned to this position.
     *
     * @return the position ID
     */
    public String getPositionId() {
        return id;
    }

    /**
     * Sets the unique ID used to identify this position.
     *
     * @param newPositionId the new position ID
     */
    public void setPositionId(String newPositionId) {
        this.id = newPositionId;
    }

    /**
     * Retrieves the title of this position as shown in OHS records.
     *
     * @return the position title
     */
    public String getPositionName() {
        return positionName;
    }

    /**
     * Sets the title of this position.
     *
     * @param newPositionName the new position title
     */
    public void setPositionName(String newPositionName) {
        this.positionName = newPositionName;
    }

    /**
     * Retrieves the category this position is grouped under.
     *
     * @return the position category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets this position to the given category.
     *
     * @param newCategory the category to group this position under
     */
    public void setCategory(String newCategory) {
        this.category = newCategory;
    }

    /**
     * Retrieves the department this position belongs to.
     *
     * @return the owning department
     */
    public Department getDepartment() {
        return department;
    }

    /**
     * Sets this position to the given department.
     *
     * @param newDepartment the department to assign
     */
    public void setDepartment(Department newDepartment) {
        this.department = newDepartment;
    }
}
