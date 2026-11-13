package com.rocs.rc.ohs.data.connection;

import com.rocs.rc.ohs.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections.
 */
public final class ConnectionHelper {

    private ConnectionHelper() {
        // Prevent instantiation
    }

    /**
     * Establishes and returns a database connection.
     *
     * @return A valid database connection.
     * @throws SQLException If connection fails or driver is missing.
     */
    public static Connection getConnection() throws SQLException {
        try {
            String driver = AppConfig.getRequired("db.driver");
            String url = AppConfig.getRequired("db.url");
            String username = AppConfig.getRequired("db.username");
            String password = AppConfig.getRequired("db.password");

            Class.forName(driver);
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found.", e);
        }
    }
}
