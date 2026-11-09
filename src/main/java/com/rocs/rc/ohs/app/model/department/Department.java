package com.rocs.rc.ohs.app.model.department;

/**
 * Represents a department in the system.
 */
public class Department {

    /** The unique ID of the department. */
    private Long id;

    /** The name of the department. */
    private String departmentName;

    /**
     * Retrieves the unique ID assigned to this department.
     *
     * @return the department ID
     */
    public Long getDepartmentId() {
        return id;
    }

    /**
     * Sets the unique ID used to identify this department.
     *
     * @param newDepartmentId the new department ID
     */
    public void setDepartmentId(Long newDepartmentId) {
        this.id = newDepartmentId;
    }

    /**
     * Retrieves the department name as shown in OHS records.
     *
     * @return the department name
     */
    public String getDepartmentName() {
        return departmentName;
    }

    /**
     * Sets the department name that appears in OHS records.
     *
     * @param newDepartmentName the new department name
     */
    public void setDepartmentName(String newDepartmentName) {
        this.departmentName = newDepartmentName;
    }
}
