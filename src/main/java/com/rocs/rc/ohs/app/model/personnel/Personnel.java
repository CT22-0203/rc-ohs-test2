package com.rocs.rc.ohs.app.model.personnel;

import com.rocs.rc.ohs.app.model.person.Person;

/**
 * Represents a clinic personnel in the system.
 */
public class Personnel {

    /** The unique ID of the personnel. */
    private Long id;

    /** The person associated with the personnel. */
    private Person person;

    /** The employee ID of the personnel. */
    private String employeeId;

    /**
     * Retrieves the unique ID of the personnel.
     *
     * @return the unique ID of the personnel.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique ID of the personnel.
     *
     * @param newId the unique ID of the personnel.
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Retrieves the person associated with the personnel.
     *
     * @return the person associated with the personnel.
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Sets the person associated with the personnel.
     *
     * @param newPerson the person associated with the personnel.
     */
    public void setPerson(Person newPerson) {
        this.person = newPerson;
    }

    /**
     * Retrieves the employee ID of the personnel.
     *
     * @return the employee ID of the personnel.
     */
    public String getEmployeeId() {
        return employeeId;
    }

    /**
     * Sets the employee ID of the personnel.
     *
     * @param newEmployeeId the employee ID of the personnel.
     */
    public void setEmployeeId(String newEmployeeId) {
        this.employeeId = newEmployeeId;
    }
}
