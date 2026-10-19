package com.studysync.util;

import com.studysync.exception.DataAccessException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database utility providing JDBC connections and transaction management.
 * 
 * CONCEPT: JDBC (Connection Management, Driver Loading, and Transactions)
 * WHY: Explicitly loads com.mysql.cj.jdbc.Driver to guarantee runtime driver registration
 * across diverse servlet containers, and encapsulates transaction begin/commit/rollback
 * for atomic multi-statement operations.
 */
public final class DBUtil {

    private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DRIVER_CLASS);
            LOGGER.info("MySQL JDBC Driver registered successfully: " + DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to load MySQL JDBC Driver: " + DRIVER_CLASS, e);
        }
    }

    private DBUtil() {
        // Utility class
    }

    /**
     * Obtains a standard JDBC Connection using configuration from AppConfig.
     * 
     * @return active java.sql.Connection
     * @throws DataAccessException if connection establishment fails
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                AppConfig.getDbUrl(),
                AppConfig.getDbUser(),
                AppConfig.getDbPassword()
            );
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not open database connection to: " + AppConfig.getDbUrl(), e);
            throw new DataAccessException("Database connectivity error. Please ensure MySQL is running.", e);
        }
    }

    /**
     * Begins a transaction on the connection by disabling auto-commit.
     * 
     * @param conn the connection to start a transaction on
     */
    public static void beginTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(false);
            } catch (SQLException e) {
                throw new DataAccessException("Failed to begin transaction", e);
            }
        }
    }

    /**
     * Commits the current transaction and resets auto-commit to true.
     * 
     * @param conn the connection to commit
     */
    public static void commitTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                throw new DataAccessException("Failed to commit transaction", e);
            }
        }
    }

    /**
     * Rolls back the current transaction safely without throwing unhandled exceptions.
     * 
     * @param conn the connection to rollback
     */
    public static void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to rollback transaction", e);
            }
        }
    }

    /**
     * Safely closes a connection, logging any warning.
     */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing connection", e);
            }
        }
    }
}
