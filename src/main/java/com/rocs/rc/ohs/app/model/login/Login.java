package com.rocs.rc.ohs.app.model.login;

import java.util.Date;

/**
 * Represents a login account in the system.
 */
public class Login {

    /** The unique ID of the login user. */
    private Long id;

    /** The username used to log in. */
    private String username;

    /** The password used to log in. */
    private String password;

    /** The date and time of the user's last login. */
    private Date lastLoginDate;

    /** The role of the user (for example, admin or student). */
    private String role;

    /**
     * Retrieves the ID of the login user.
     *
     * @return The ID of the login user.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the ID of the login user.
     *
     * @param newId The ID of the login user.
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Retrieves the username of the login user.
     *
     * @return The username of the login user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the login user.
     *
     * @param newUsername The username of the login user.
     */
    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    /**
     * Retrieves the password of the login user.
     *
     * @return The password of the login user.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the login user.
     *
     * @param newPassword The password of the login user.
     */
    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * Retrieves the date of the user's last login.
     *
     * @return The date of the last login.
     */
    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    /**
     * Sets the date of the user's last login.
     *
     * @param newLastLoginDate The date of the last login.
     */
    public void setLastLoginDate(Date newLastLoginDate) {
        this.lastLoginDate = newLastLoginDate;
    }

    /**
     * Retrieves the role of the login user.
     *
     * @return The role of the login user.
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role of the login user.
     *
     * @param newRole The role of the login user.
     */
    public void setRole(String newRole) {
        this.role = newRole;
    }
}
