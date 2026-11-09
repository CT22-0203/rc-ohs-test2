package com.rocs.rc.ohs.app.model.student;


import com.rocs.rc.ohs.app.model.person.Person;

/**
 * Represents a student in the system.
 */
public class Student {

    /** The unique ID of the student. */
    private String id;

    /** The student number. */
    private String studentNumber;

    /** The person associated with the student. */
    private Person person;


    /**
     * Retrieves the unique ID of this student record.
     *
     * @return the student ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of this student record.
     *
     * @param newId the new student ID
     */
    public void setId(String newId) {
        this.id = newId;
    }

    /**
     * Retrieves the person details linked to this student.
     *
     * @return the linked person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Sets this student to the given person details.
     *
     * @param newPerson the person to link
     */
    public void setPerson(Person newPerson) {
        this.person = newPerson;
    }

    /**
     * Retrieves the student number issued by the school.
     *
     * @return the student number
     */
    public String getStudentNumber() {
        return studentNumber;
    }

    /**
     * Sets the student number issued by the school.
     *
     * @param newStudentNumber the new student number
     */
    public void setStudentNumber(String newStudentNumber) {
        this.studentNumber = newStudentNumber;
    }
}
