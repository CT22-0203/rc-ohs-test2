package com.rocs.rc.ohs.app.model.enums.gender;

/**
 * Represents the gender of a person.
 */
public enum Gender {

    /** Represents the male gender. */
    MALE("Male"),

    /** Represents the female gender. */
    FEMALE("Female");

    /** The corresponding database value. */
    private final String value;

    /**
     * Creates a gender with its corresponding database value.
     *
     * @param newValue the database value of the gender.
     */
    Gender(String newValue) {
        this.value = newValue;
    }

    /**
     * Retrieves the database value of the gender.
     *
     * @return the database value of the gender.
     */
    public String getValue() {
        return value;
    }
}
