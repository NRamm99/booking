package com.booking.booking.repository;

import com.booking.booking.util.AppLogger;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DB_URL = "jdbc:sqlite:booking.db";
    private Connection connection;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public void initialize() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            createSchema();
            seedDefaultData();
            AppLogger.info("Database ready at: booking.db");
        } catch (SQLException e) {
            AppLogger.error("Database init failed: " + e.getMessage());
            throw new RuntimeException("Could not initialize database", e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                connection.setAutoCommit(true);
                AppLogger.warn("Database reconnected.");
            }
        } catch (SQLException e) {
            AppLogger.error("Failed to get DB connection: " + e.getMessage());
            throw new RuntimeException("Database connection error", e);
        }
        return connection;
    }

    private void createSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS employees (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    email TEXT,
                    phone TEXT,
                    role TEXT NOT NULL DEFAULT 'EMPLOYEE'
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS customers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    email TEXT,
                    phone TEXT,
                    notes TEXT,
                    created_at DATETIME DEFAULT (datetime('now'))
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS treatments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    duration_minutes INTEGER NOT NULL,
                    price REAL NOT NULL
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS appointments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER NOT NULL,
                    employee_id INTEGER NOT NULL,
                    treatment_id INTEGER NOT NULL,
                    start_time DATETIME NOT NULL,
                    end_time DATETIME NOT NULL,
                    status TEXT NOT NULL DEFAULT 'ACTIVE',
                    has_payed INTEGER NOT NULL DEFAULT 0,
                    notes TEXT,
                    created_at DATETIME DEFAULT (datetime('now')),
                    FOREIGN KEY (customer_id) REFERENCES customers(id),
                    FOREIGN KEY (employee_id) REFERENCES employees(id),
                    FOREIGN KEY (treatment_id) REFERENCES treatments(id)
                )
            """);
            try {
                stmt.executeUpdate("""
                    ALTER TABLE appointments
                    ADD COLUMN has_payed INTEGER NOT NULL DEFAULT 0
                """);
            } catch (SQLException e) {
                // Existing databases already have the column after first migration.
                if (e.getMessage() == null || !e.getMessage().contains("duplicate column name: has_payed")) {
                    throw e;
                }
            }

            // Index for conflict detection
            stmt.executeUpdate("""
                CREATE INDEX IF NOT EXISTS idx_appt_employee_time
                ON appointments(employee_id, start_time, end_time)
            """);
            AppLogger.info("Schema created/verified.");
        }
    }

    private void seedDefaultData() throws SQLException {
        // Only seed if empty
        var rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM employees");
        if (rs.next() && rs.getInt(1) == 0) {
            String hash = BCrypt.hashpw("admin123", BCrypt.gensalt());
            connection.createStatement().executeUpdate(
                "INSERT INTO employees (username, password_hash, full_name, email, role) " +
                "VALUES ('monika', '" + hash + "', 'Monika Hansen', 'monika@harmoni-ka.dk', 'ADMIN')"
            );

            String empHash = BCrypt.hashpw("medarbejder", BCrypt.gensalt());
            connection.createStatement().executeUpdate(
                "INSERT INTO employees (username, password_hash, full_name, email, role) " +
                "VALUES ('sara', '" + empHash + "', 'Sara Nielsen', 'sara@harmoni-ka.dk', 'EMPLOYEE')"
            );

            // Default treatments
            connection.createStatement().executeUpdate("""
                INSERT INTO treatments (name, duration_minutes, price) VALUES
                ('Klipning (dame)', 60, 450),
                ('Klipning (herre)', 30, 250),
                ('Klipning (barn)', 30, 200),
                ('Farvning (hel)', 120, 950),
                ('Farvning (striber)', 90, 750),
                ('Permanent', 120, 850),
                ('Skyl og føn', 45, 300),
                ('Behandling', 60, 500)
            """);

            AppLogger.info("Default seed data inserted.");
        }
    }

  // GDPR eller skatteregler: Her sletter vi efter 5 år. IKKE IMPLEMENTERET!!!!!!
    public void purgeExpiredAppointments() {
        String sql = """
            DELETE FROM appointments
            WHERE date(created_at) < date('now', '-5 years')
        """;
        try (Statement stmt = connection.createStatement()) {
            int deleted = stmt.executeUpdate(sql);
            if (deleted > 0) {
                AppLogger.info("GDPR/Skat purge: deleted " + deleted + " appointments older than 5 years.");
            }
        } catch (SQLException e) {
            AppLogger.error("Purge failed: " + e.getMessage());
        }
    }
}
