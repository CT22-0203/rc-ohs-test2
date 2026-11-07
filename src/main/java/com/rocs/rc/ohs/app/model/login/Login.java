package com.rocs.rc.ohs.app.model.login;

import java.util.Date;

/**
 * This class represents a Login entity in the clinic application.
 * It serves as a robust, comprehensive, and efficient data model that
 * encapsulates all the essential attributes of a login account, ensuring
 * seamless integration with the rest of the application.
 */
public class Login {

    // The unique identifier of the login
    private Long id;

    // The username of the login
    private String username;

    // The password of the login
    private String password;

    // The last login date of the login
    private Date lastLoginDate;

    // The role of the login
    private String role;

    /**
     * This method returns the id of the login.
     *
     * @return The id of the login.
     */
    public Long getId() {
        // Return the id
        return id;
    }

    /**
     * This method sets the id of the login.
     *
     * @param newId The newId parameter representing the new id.
     */
    public void setId(Long newId) {
        // Validate that the id is not null
        if (newId == null) {
            // Throw an exception if the id is null
            throw new IllegalArgumentException("❌ The id must not be null");
        }
        // Set the id
        this.id = newId;
    }

    /**
     * This method returns the username of the login.
     *
     * @return The username of the login.
     */
    public String getUsername() {
        // Return the username
        return username;
    }

    /**
     * This method sets the username of the login.
     *
     * @param newUsername The newUsername parameter representing the new username.
     */
    public void setUsername(String newUsername) {
        // Validate that the username is not null or empty
        if (newUsername == null || newUsername.trim().isEmpty()) {
            // Throw an exception if the username is invalid
            throw new IllegalArgumentException("❌ The username must not be empty");
        }
        // Set the username
        this.username = newUsername;
    }

    /**
     * This method returns the password of the login.
     *
     * @return The password of the login.
     */
    public String getPassword() {
        // Return the password
        return password;
    }

    /**
     * This method sets the password of the login.
     *
     * @param newPassword The newPassword parameter representing the new password.
     */
    public void setPassword(String newPassword) {
        // Validate that the password is not null or empty
        if (newPassword == null || newPassword.trim().isEmpty()) {
            // Throw an exception if the password is invalid
            throw new IllegalArgumentException("❌ The password must not be empty");
        }
        // Set the password
        this.password = newPassword;
    }

    /**
     * This method returns the last login date of the login.
     *
     * @return The last login date of the login.
     */
    public Date getLastLoginDate() {
        // Return the last login date
        return lastLoginDate;
    }

    /**
     * This method sets the last login date of the login.
     *
     * @param newLastLoginDate The newLastLoginDate parameter representing the new date.
     */
    public void setLastLoginDate(Date newLastLoginDate) {
        // Set the last login date
        this.lastLoginDate = newLastLoginDate;
    }

    /**
     * This method returns the role of the login.
     *
     * @return The role of the login.
     */
    public String getRole() {
        // Return the role
        return role;
    }

    /**
     * This method sets the role of the login.
     *
     * @param newRole The newRole parameter representing the new role.
     */
    public void setRole(String newRole) {
        // Validate that the role is not null or empty
        if (newRole == null || newRole.trim().isEmpty()) {
            // Throw an exception if the role is invalid
            throw new IllegalArgumentException("❌ The role must not be empty");
        }
        // Set the role
        this.role = newRole;
    }
}