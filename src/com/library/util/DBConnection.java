package com.library.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates JDBC connections.
 * The URL, user and password are read from 'db.properties' (project root),
 * so you can change the database settings WITHOUT recompiling.
 */
public class DBConnection {
    private static String url;
    private static String user;
    private static String password;

    // 'static block' runs once, when the class is first loaded
    static {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) {
            props.load(in);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");
        } catch (IOException e) {
            System.out.println("ERROR: Cannot read db.properties. Run the program from the project root folder.");
        }
    }

    /** Opens a NEW connection every time it is called. Caller must close it. */
    public static Connection getConnection() throws SQLException {
        if (url == null) {
            throw new SQLException("Database settings are missing. Check db.properties.");
        }
        // MySQL Connector/J 8+ registers its driver automatically (no Class.forName needed)
        return DriverManager.getConnection(url, user, password);
    }

    /** Undo all changes made in the current transaction (used in catch blocks). */
    public static void rollbackQuietly(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }
        }
    }

    /** Restore auto-commit and close the connection (used in finally blocks). */
    public static void closeQuietly(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException ex) {
                System.out.println("Could not close connection: " + ex.getMessage());
            }
        }
    }
}
