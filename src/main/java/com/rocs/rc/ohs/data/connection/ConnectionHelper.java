package com.rocs.rc.ohs.data.connection;

import com.rocs.rc.ohs.config.ApplicationProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Provides database connections for the application.
 *
 * <p>The database vendor is configured with {@code db.vendor} in
 * {@code application.properties}, next to {@code build.properties}.
 * Supported values are {@code oracle} and
 * {@code mysql}; the matching {@code db.<vendor>.*} block supplies the JDBC
 * driver, URL and credentials. A deployment can override any value with a
 * JVM system property, for example:</p>
 *
 * <pre>{@code java -Ddb.vendor=mysql -jar build/dist/rc-ohs.jar}</pre>
 */
public final class ConnectionHelper {

    /** Property key that specifies the database vendor. */
    private static final String VENDOR_KEY = "db.vendor";

    /** Vendor used when {@code db.vendor} is not set or is blank. */
    private static final String DEFAULT_VENDOR = "oracle";

    /** Vendor name for Oracle databases. */
    private static final String ORACLE = "oracle";

    /** Vendor name for MySQL databases. */
    private static final String MYSQL = "mysql";

    /**
     * Prevents instantiation of this utility class.
     */
    private ConnectionHelper() {
    }

    /**
     * Opens a connection to the configured database.
     *
     * @return A {@link Connection} to the selected database.
     * @throws IllegalStateException If the configuration is invalid, the JDBC
     *                               driver is missing, or the connection
     *                               cannot be established.
     */
    public static Connection getConnection() {

        String vendor = getVendor();
        String prefix = "db." + vendor + ".";

        String driver = ApplicationProperties.getRequired(prefix + "driver");
        String url = ApplicationProperties.getRequired(prefix + "url");
        String username = valueOrEmpty(prefix + "username");
        String password = valueOrEmpty(prefix + "password");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                    "JDBC driver not found for vendor \"" + vendor + "\": "
                            + driver,
                    ex
            );
        }

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Error connecting to the " + vendor + " database.",
                    ex
            );
        }
    }

    /**
     * Returns the database vendor currently selected by the configuration.
     *
     * @return Either {@code oracle} or {@code mysql}.
     * @throws IllegalStateException If the configured vendor is not supported.
     */
    public static String getVendor() {

        String vendor = ApplicationProperties.get(VENDOR_KEY);

        if (vendor == null || vendor.trim().isEmpty()) {
            vendor = DEFAULT_VENDOR;
        }

        vendor = vendor.trim().toLowerCase(Locale.ROOT);

        if (!ORACLE.equals(vendor) && !MYSQL.equals(vendor)) {
            throw new IllegalStateException(
                    "Unsupported " + VENDOR_KEY + " value: \"" + vendor
                            + "\". Use \"" + ORACLE + "\" or \"" + MYSQL
                            + "\"."
            );
        }

        return vendor;
    }

    /**
     * Reads an optional property value.
     *
     * @param key The property key to look up.
     * @return The property value, or an empty string if it is not set.
     */
    private static String valueOrEmpty(String key) {

        String value = ApplicationProperties.get(key);

        return value == null ? "" : value;
    }
}
