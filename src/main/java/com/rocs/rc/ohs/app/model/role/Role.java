package com.rocs.rc.ohs.app.model.role;

/**
 * Represents a role in the system.
 */
public class Role {

    /** The unique ID of the role. */
    private Long id;

    /** The name of the role. */
    private String role;

    /** The description of the role. */
    private String description;


    /**
     * Retrieves the unique ID of this role.
     *
     * @return the role ID
     */
    public Long getId() {
        return id;
    }


    /**
     * Sets the unique ID of this role.
     *
     * @param newId the new role ID
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Retrieves the name of this role.
     *
     * @return the role name
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the name of this role.
     *
     * @param newRole the new role name
     */
    public void setRole(String newRole) {
        this.role = newRole;
    }

    /**
     * Retrieves the description of what this role is used for.
     *
     * @return the role description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of what this role is used for.
     *
     * @param newDescription the new role description
     */
    public void setDescription(String newDescription) {
        this.description = newDescription;
    }
}
