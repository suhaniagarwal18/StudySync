package com.studysync.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton configuration loader that reads application.properties from the classpath
 * and supports environment-variable overrides for production / container environments.
 * 
 * CONCEPT: Encapsulation & Configuration Management
 */
public final class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                PROPERTIES.load(in);
                LOGGER.info("Successfully loaded application.properties from classpath.");
            } else {
                LOGGER.warning("application.properties not found on classpath, relying on defaults and environment.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load application.properties", e);
        }
    }

    private AppConfig() {
        // Prevent instantiation of utility class
    }

    /**
     * Retrieves a property with environment variable override support.
     * E.g. key "db.password" checks env var "STUDYSYNC_DB_PASSWORD" first.
     */
    public static String getProperty(String key, String defaultValue) {
        String envKey = "STUDYSYNC_" + key.replace('.', '_').toUpperCase();
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static String getDbUrl() {
        return getProperty("db.url", "jdbc:mysql://localhost:3306/studysync?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    }

    public static String getDbUser() {
        return getProperty("db.user", "root");
    }

    public static String getDbPassword() {
        return getProperty("db.password", "root");
    }

    public static long getMaxUploadBytes() {
        String val = getProperty("upload.maxBytes", "10485760"); // 10 MB default
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return 10 * 1024 * 1024L;
        }
    }

    /**
     * Resolves the secure uploads directory path.
     * Stored safely OUTSIDE the webapp root under the user's home directory.
     */
    public static Path getUploadDirectory() {
        String customDir = getProperty("upload.dir", "studysync-uploads");
        Path path = Paths.get(customDir);
        if (!path.isAbsolute()) {
            String userHome = System.getProperty("user.home", ".");
            path = Paths.get(userHome, customDir);
        }
        return path.normalize();
    }
}
