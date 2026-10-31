package com.rocs.rc.ohs.data.dao.login;

import com.rocs.rc.ohs.app.model.login.Login;

/**
 * Defines the data access operations for login accounts.
 */
public interface LoginDao {

    /**
     * Retrieves the login account that matches the given username.
     *
     * @param username The username of the account to look up.
     * @return The {@link Login} account of the user.
     */
    Login login(String username);
}
