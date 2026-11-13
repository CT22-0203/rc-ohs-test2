package com.rocs.rc.ohs.data.dao.login.impl;

import com.rocs.rc.ohs.app.model.login.Login;
import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import com.rocs.rc.ohs.data.dao.login.LoginDao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.rocs.rc.ohs.data.utils.QueryConstants.SELECT_LOGIN_BY_USERNAME;
import static com.rocs.rc.ohs.data.utils.QueryConstants.UPDATE_LAST_LOGIN_DATE;
import static com.rocs.rc.ohs.data.utils.QueryConstants.UPDATE_PASSWORD_BY_EMAIL_AND_USERNAME;
/**
 * Implements the data access operations for login accounts
 * and for resetting a forgotten password.
 */
public class LoginDaoImpl implements LoginDao {

    /**
     * Retrieves the login account that
     * matches the given username
     * and updates its last login date.
     *
     * @param username The username of the account to look up.
     * @return The {@link Login} account, or null if it is not found.
     */
    @Override
    public Login login(String username) {

        Login login = null;
        try (Connection conn = ConnectionHelper.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                            SELECT_LOGIN_BY_USERNAME);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                login = new Login();
                login.setUsername(rs.getString("username"));
                login.setPassword(rs.getString("password"));

                String id = rs.getString("id");
                PreparedStatement stmt2 = conn.prepareStatement(
                        UPDATE_LAST_LOGIN_DATE);
                stmt2.setDate(1, new Date(System.currentTimeMillis()));
                stmt2.setString(2, id);

                stmt2.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println(
                    "An exception was thrown while finding an item. "
                            + e.getMessage());
        }
        return login;
    }

    @Override
    public boolean updatePassword(String email,
                                  String username, String newPassword) {
        if (email == null || email.trim().isEmpty()
                || username == null || newPassword == null) {
            return false;
        }
        String value = email.trim();

        try (Connection connection = ConnectionHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     UPDATE_PASSWORD_BY_EMAIL_AND_USERNAME)) {
            statement.setString(1, newPassword);
            statement.setString(2, username.trim());
            statement.setString(3, value);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating password: " + e.getMessage());
            return false;
        }
    }
}
