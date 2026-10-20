package com.rocs.rc.ohs.data.dao.login.impl;


import com.rocs.rc.ohs.app.model.login.Login;
import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import com.rocs.rc.ohs.data.dao.login.LoginDao;

import java.sql.*;

public class LoginDaoImpl implements LoginDao {


    @Override
    public Login login (String username) {

        Login login = null;
        try (Connection conn = ConnectionHelper.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("select * from login where username = ?");
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                login = new Login();
                login.setUsername(rs.getString("username"));
                login.setPassword(rs.getString("password"));

                Long id = rs.getLong("id");
                PreparedStatement stmt2 = conn.prepareStatement("update login set last_login_date = ? where id = ?");
                stmt2.setDate(1, new Date(System.currentTimeMillis()));
                stmt2.setLong(2, id);

                stmt2.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println("An exception was thrown while finding an item. " + e.getMessage());
        }
        return login;
    }
}
