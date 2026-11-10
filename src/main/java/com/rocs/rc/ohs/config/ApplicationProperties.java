package com.rocs.rc.ohs.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Loads {@code application.properties} from the classpath.
 *
 * <p>When the file is not on the classpath (for example when running from an
 * IDE that did not run the Ant copy step) the copy in the working directory,
 * next to {@code build.properties}, is used instead.</p>
 *
 * <p>A JVM system property with the same name takes precedence, so a
 * deployment can override a value without rebuilding, for example
 * {@code -Ddb.vendor=mysql}.</p>
 */
public final class ApplicationProperties {

    /** Name of the properties file to load. */
    private static final String FILE_NAME = "application.properties";

    /** Vendor used when {@code db.vendor} is not set or is blank. */
    private static final Properties VALUES = load();

    /**
     * Prevents instantiation of this utility class.
     */
    private ApplicationProperties() {
    }

    /**
     * Returns the value for a key, or {@code null} when it is not defined.
     *
     * @param key The property name.
     * @return The configured value, or {@code null}.
     */
    public static String get(String key) {

        String override = System.getProperty(key);

        if (override != null) {
            return override;
        }

        return VALUES.getProperty(key);
    }

    /**
     * Returns the value for a key and rejects missing or blank values.
     *
     * @param key The property name.
     * @return The configured value, trimmed.
     * @throws IllegalStateException If the key is missing or blank.
     */
    public static String getRequired(String key) {

        String value = get(key);

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing required property \"" + key + "\" in " + FILE_NAME
            );
        }

        return value.trim();
    }

    /**
     * Loads {@code application.properties}.
     *
     * <p>The classpath is checked first. If the file is not found there, the
     * copy in the working directory is used instead.</p>
     *
     * @return The loaded properties.
     * @throws IllegalStateException If the file cannot be read, or is not found
     *                               on the classpath or in the working
     *                               directory.
     */
    private static Properties load() {

        Properties properties = new Properties();

        try (InputStream stream = ApplicationProperties.class.getClassLoader()
                .getResourceAsStream(FILE_NAME)) {

            if (stream != null) {
                properties.load(stream);
                return properties;
            }
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Could not read " + FILE_NAME + " from the classpath.",
                    ex
            );
        }

        Path source = Paths.get(FILE_NAME);

        if (Files.isReadable(source)) {
            try (InputStream stream = Files.newInputStream(source)) {
                properties.load(stream);
            } catch (IOException ex) {
                throw new IllegalStateException(
                        "Could not read " + FILE_NAME + ".",
                        ex
                );
            }
        }

        if (properties.isEmpty()) {
            throw new IllegalStateException(
                    FILE_NAME + " was not found on the classpath or in "
                            + Paths.get("").toAbsolutePath()
            );
        }

        return properties;
    }
}
