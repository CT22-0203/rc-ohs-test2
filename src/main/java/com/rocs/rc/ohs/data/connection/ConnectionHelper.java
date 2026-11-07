package com.rocs.rc.ohs.data.connection;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Provides database connections for the application.
 */
public final class ConnectionHelper {

    /** The JDBC URL of the database. */
    public static final String URL = "jdbc:oracle:thin:@localhost:1521:rogate";

    /** The JDBC driver class name. */
    public static final String ORACLE_DRIVER =
            "oracle.jdbc.driver.OracleDriver";

    /** The database username. */
    public static final String USERNAME = "rcis";

    /** The database password. */
    public static final String PASSWORD = "Changeme0";

    /**
     * Prevents instantiation of this utility class.
     */
    private ConnectionHelper() {
    }

    /**
     * Opens a connection to the database.
     *
     * @return A {@link Connection} to the database.
     */
    public static Connection getConnection() {
        try {
            Class.forName(ORACLE_DRIVER).newInstance();
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (Exception ex) {
            throw new RuntimeException("Error connecting to the database.", ex);
        }
    }

}
