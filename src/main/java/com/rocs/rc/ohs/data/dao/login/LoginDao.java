package com.rocs.rc.ohs.data.dao.login;

import com.rocs.rc.ohs.app.model.login.Login;

/**
 * Defines the data access operations for login accounts
 * and for resetting a forgotten password.
 */

public interface LoginDao {
    /**
     * Retrieves the login account that matches the given username.
     *
     * @param username The username of the account to look up.
     * @return The {@link Login} account of the user.
     */

    Login login(String username);

    /**
     * Updates the password only if the email and the
     * username belong to the same account.
     *
     * @param email The email address.
     * @param username The username entered by the user.
     * @param newPassword The new password.
     * @return True if the password was updated, false if nothing matched.
     */

    boolean updatePassword(String email, String username,
                           String newPassword);
}

