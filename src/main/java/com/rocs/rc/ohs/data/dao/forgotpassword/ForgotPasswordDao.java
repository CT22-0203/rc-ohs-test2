package com.rocs.rc.ohs.data.dao.forgotpassword;

/**
 * Defines the data access operations for resetting a forgotten password.
 */
public interface ForgotPasswordDao {

    /**
     * Checks if the Staff ID or email belongs to an existing account.
     *
     * @param staffIdOrEmail The Staff ID or the email address.
     * @return True if an account matches, false if blank or not found.
     */
    boolean isRegistered(String staffIdOrEmail);

    /**
     * Updates the password only if the Staff ID or email and the
     * username belong to the same account.
     *
     * @param staffIdOrEmail The Staff ID or the email address.
     * @param username The username entered by the user.
     * @param newPassword The new password.
     * @return True if the password was updated, false if nothing matched.
     */
    boolean updatePassword(String staffIdOrEmail, String username,
                           String newPassword);
}