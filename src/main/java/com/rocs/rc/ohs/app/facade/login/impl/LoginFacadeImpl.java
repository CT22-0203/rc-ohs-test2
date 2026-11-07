package com.rocs.rc.ohs.app.facade.login.impl;


import com.rocs.rc.ohs.app.facade.login.LoginFacade;
import com.rocs.rc.ohs.app.model.login.Login;
import com.rocs.rc.ohs.data.dao.login.LoginDao;
import com.rocs.rc.ohs.data.dao.login.impl.LoginDaoImpl;

/**
 * Implements the login operations of the system.
 */
public class LoginFacadeImpl implements LoginFacade {

    /** The data access object used to retrieve login accounts. */
    private final LoginDao loginDao = new LoginDaoImpl();

    /**
     * Authenticates a user using the given credentials.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return The {@link Login} account of the user.
     */
    @Override
    public Login login(String username, String password) {
        return this.loginDao.login(username);
    }
}
