package com.rocs.rc.ohs.data.dao.forgotpassword.impl;

import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import com.rocs.rc.ohs.data.dao.forgotpassword.ForgotPasswordDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implements the data access operations for resetting a forgotten password.
 */
public class ForgotPasswordDaoImpl implements ForgotPasswordDao {

    /**
     * Checks if the Staff ID or email belongs to an existing account.
     *
     * @param staffIdOrEmail The Staff ID or the email address.
     * @return True if an account matches, false if blank or not found.
     */
    @Override
    public boolean isRegistered(String staffIdOrEmail) {

        boolean registered = false;
        if (staffIdOrEmail != null && !staffIdOrEmail.trim().isEmpty()) {
            String value = staffIdOrEmail.trim();
            try (Connection conn = ConnectionHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "select l.id from login l "
                                 + "join person pe on pe.id = l.person_id "
                                 + "left join personnel p "
                                 + "on p.person_id = pe.id "
                                 + "where lower(p.employee_id) = lower(?) "
                                 + "or lower(pe.email) = lower(?)")) {
                stmt.setString(1, value);
                stmt.setString(2, value);

                try (ResultSet rs = stmt.executeQuery()) {
                    registered = rs.next();
                }
            } catch (SQLException e) {
                System.out.println(
                        "An exception was thrown while finding an item. "
                                + e.getMessage());
            }
        }
        return registered;
    }

    /**
     * Updates the password only if the Staff ID or email and the username
     * belong to the same account.
     *
     * @param staffIdOrEmail The Staff ID or the email address.
     * @param username The username entered by the user.
     * @param newPassword The new password.
     * @return True if the password was updated, false if nothing matched.
     */
    @Override
    public boolean updatePassword(String staffIdOrEmail, String username,
                                  String newPassword) {

        boolean updated = false;
        if (staffIdOrEmail != null && !staffIdOrEmail.trim().isEmpty()
                && username != null && newPassword != null) {
            String value = staffIdOrEmail.trim();
            try (Connection conn = ConnectionHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "update login l "
                                 + "join person pe on pe.id = l.person_id "
                                 + "left join personnel p "
                                 + "on p.person_id = pe.id "
                                 + "set l.password = ? "
                                 + "where l.username = ? "
                                 + "and (lower(p.employee_id) = lower(?) "
                                 + "or lower(pe.email) = lower(?))")) {
                stmt.setString(1, newPassword);
                stmt.setString(2, username.trim());
                stmt.setString(3, value);
                stmt.setString(4, value);

                updated = stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.out.println(
                        "An exception was thrown while updating an item. "
                                + e.getMessage());
            }
        }
        return updated;
    }
}