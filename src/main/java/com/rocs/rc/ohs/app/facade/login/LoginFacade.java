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
}
