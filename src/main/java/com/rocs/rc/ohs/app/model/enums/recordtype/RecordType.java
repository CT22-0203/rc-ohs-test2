package com.rocs.rc.ohs.app.model.enums.recordtype;

/**
 * Represents the type of consultation record.
 */
public enum RecordType {

    /** Represents a check-up consultation. */
    CHECK_UP("Check-up"),

    /** Represents a follow-up consultation. */
    FOLLOW_UP("Follow-up");

    /** The corresponding database value. */
    private final String value;

    /**
     * Creates a record type with its corresponding database value.
     *
     * @param newValue the database value of the record type.
     */
    RecordType(String newValue) {
        this.value = newValue;
    }

    /**
     * Retrieves the database value of the record type.
     *
     * @return the database value of the record type.
     */
    public String getValue() {
        return value;
    }
}
