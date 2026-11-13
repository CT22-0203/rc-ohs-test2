package com.rocs.rc.ohs.app.facade.login;

import com.rocs.rc.ohs.app.model.login.Login;

/**
 * Defines the login operations of the system.
 */
public interface LoginFacade {

    /**
     * Authenticates a user using the given credentials.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return The {@link Login} account of the authenticated user.
     */
    Login login(String username, String password);

    /**
     * Resets the password of an account after
     * its details are verified.
     *
     * @param email The email address of the account owner.
     * @param username The username of the account.
     * @param newPassword The new password.
     * @return True if the password was updated, false otherwise.
     * @throws IllegalArgumentException If a required value is missing.
     */
    boolean forgotPassword(String email, String username,
                           String newPassword)
            throws IllegalArgumentException;
}

