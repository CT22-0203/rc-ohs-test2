package com.rocs.rc.ohs.app.model.person;

import com.rocs.rc.ohs.app.model.enums.gender.Gender;

import java.time.LocalDateTime;

/**
 * Represents a person in the system.
 */
public class Person {

    /** The unique ID of the person. */
    private Long id;

    /** The first name of the person. */
    private String firstName;

    /** The middle name of the person. */
    private String middleName;

    /** The last name of the person. */
    private String lastName;

    /** The email address of the person. */
    private String email;

    /** The date of birth of the person. */
    private LocalDateTime dateOfBirth;

    /** The gender of the person. */
    private Gender gender;

    /** The address of the person. */
    private String address;

    /** The contact number of the person. */
    private String contactNumber;

    /**
     * Retrieves the unique ID of the person.
     *
     * @return the unique ID of the person.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique ID of the person.
     *
     * @param newId the unique ID of the person.
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Retrieves the first name of the person.
     *
     * @return the first name of the person.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the person.
     *
     * @param newFirstName the first name of the person.
     */
    public void setFirstName(String newFirstName) {
        this.firstName = newFirstName;
    }

    /**
     * Retrieves the middle name of the person.
     *
     * @return the middle name of the person.
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Sets the middle name of the person.
     *
     * @param newMiddleName the middle name of the person.
     */
    public void setMiddleName(String newMiddleName) {
        this.middleName = newMiddleName;
    }

    /**
     * Retrieves the last name of the person.
     *
     * @return the last name of the person.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the person.
     *
     * @param newLastName the last name of the person.
     */
    public void setLastName(String newLastName) {
        this.lastName = newLastName;
    }

    /**
     * Retrieves the email address of the person.
     *
     * @return the email address of the person.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the person.
     *
     * @param newEmail the email address of the person.
     */
    public void setEmail(String newEmail) {
        this.email = newEmail;
    }

    /**
     * Retrieves the date of birth of the person.
     *
     * @return the date of birth of the person.
     */
    public LocalDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets the date of birth of the person.
     *
     * @param newDateOfBirth the date of birth of the person.
     */
    public void setDateOfBirth(LocalDateTime newDateOfBirth) {
        this.dateOfBirth = newDateOfBirth;
    }

    /**
     * Retrieves the gender of the person.
     *
     * @return the gender of the person.
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Sets the gender of the person.
     *
     * @param newGender the gender of the person.
     */
    public void setGender(Gender newGender) {
        this.gender = newGender;
    }

    /**
     * Retrieves the address of the person.
     *
     * @return the address of the person.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address of the person.
     *
     * @param newAddress the address of the person.
     */
    public void setAddress(String newAddress) {
        this.address = newAddress;
    }

    /**
     * Retrieves the contact number of the person.
     *
     * @return the contact number of the person.
     */
    public String getContactNumber() {
        return contactNumber;
    }

    /**
     * Sets the contact number of the person.
     *
     * @param newContactNumber the contact number of the person.
     */
    public void setContactNumber(String newContactNumber) {
        this.contactNumber = newContactNumber;
    }
}
