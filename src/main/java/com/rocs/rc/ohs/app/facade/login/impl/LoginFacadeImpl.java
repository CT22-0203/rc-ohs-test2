package com.rocs.rc.ohs.app.facade.login.impl;

import com.rocs.rc.ohs.app.facade.login.LoginFacade;
import com.rocs.rc.ohs.app.model.login.Login;
import com.rocs.rc.ohs.data.dao.login.LoginDao;
import com.rocs.rc.ohs.data.dao.login.impl.LoginDaoImpl;

/**
 * Implements the login
 * operations of the system.
 */
public class LoginFacadeImpl implements LoginFacade {

    /**
     * The data access object used to retrieve login accounts.
     */
    private final LoginDao loginDao;

    /**
     * Creates a facade that uses the default data access object.
     */
    public LoginFacadeImpl() {
        this(new LoginDaoImpl());
    }

    /**
     * Creates a facade that uses the given data access object.
     *
     * @param newLoginDao The DAO of the login accounts.
     */
    public LoginFacadeImpl(LoginDao newLoginDao) {
        this.loginDao = newLoginDao;
    }

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

    /**
     * Resets the password of an account after checking the given details.
     *
     * @param email The email address of the account.
     * @param username The username of the account.
     * @param newPassword The new password of the account.
     * @return True if the password was updated, false otherwise.
     */
    @Override
    public boolean forgotPassword(String email, String username,
                                  String newPassword) {
        boolean success = false;
        try {
            validateForgotPassword(email, username, newPassword);
            success = loginDao.updatePassword(email, username, newPassword);
        } catch (IllegalArgumentException ex) {
            System.out.println(
                    "Cannot reset the password. " + ex.getMessage());
        }
        return success;
    }

    /**
     * Checks that every value needed to reset a password is present.
     *
     * @param email The email address to check.
     * @param username The username to check.
     * @param newPassword The new password to check.
     * @throws IllegalArgumentException If a value is missing.
     */
    private void validateForgotPassword(String email, String username,
                                        String newPassword)
            throws IllegalArgumentException {
        if (isBlank(email)) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (isBlank(username)) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (isBlank(newPassword)) {
            throw new IllegalArgumentException("New password is required.");
        }
    }

    /**
     * Tells whether a text is missing or empty.
     *
     * @param text The text to check.
     * @return True if the text is null or blank.
     */
    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}

