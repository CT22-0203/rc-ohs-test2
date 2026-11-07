package com.rocs.rc.ohs.data.dao.login.impl;


import com.rocs.rc.ohs.app.model.login.Login;
import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import com.rocs.rc.ohs.data.dao.login.LoginDao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implements the data access operations for login accounts.
 */
public class LoginDaoImpl implements LoginDao {

    /**
     * Retrieves the login account that matches the given username
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
                    "select * from login where username = ?");
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                login = new Login();
                login.setUsername(rs.getString("username"));
                login.setPassword(rs.getString("password"));

                Long id = rs.getLong("id");
                PreparedStatement stmt2 = conn.prepareStatement(
                        "update login set last_login_date = ? where id = ?");
                stmt2.setDate(1, new Date(System.currentTimeMillis()));
                stmt2.setLong(2, id);

                stmt2.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println(
                    "An exception was thrown while finding an item. "
                            + e.getMessage());
        }
        return login;
    }
}
