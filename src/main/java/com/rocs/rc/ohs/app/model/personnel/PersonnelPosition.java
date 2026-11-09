package com.rocs.rc.ohs.app.model.personnel;

/**
 * Represents the position assigned to personnel.
 */
public class PersonnelPosition {

    /** The unique ID of the personnel position. */
    private String id;

    /** The personnel assigned to the position. */
    private Personnel personnel;

    /** The position assigned to the personnel. */
    private Position position;

    /**
     * Retrieves the unique ID of this personnel position.
     *
     * @return the personnel position ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of this personnel position.
     *
     * @param newId the new personnel position ID
     */
    public void setId(String newId) {
        this.id = newId;
    }

    /**
     * Retrieves the personnel assigned to this position.
     *
     * @return the assigned personnel
     */
    public Personnel getPersonnel() {
        return personnel;
    }

    /**
     * Sets the given personnel to this position.
     *
     * @param newPersonnel the personnel to assign
     */
    public void setPersonnel(Personnel newPersonnel) {
        this.personnel = newPersonnel;
    }

    /**
     * Retrieves the position assigned to the personnel.
     *
     * @return the assigned position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets the given position to the personnel.
     *
     * @param newPosition the position to assign
     */
    public void setPosition(Position newPosition) {
        this.position = newPosition;
    }
}
