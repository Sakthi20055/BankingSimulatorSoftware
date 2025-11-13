package com.bank.simulator.service;

import java.sql.*;

public class DatabaseService {

    // ⚙️ Update your connection details as needed
    private static final String URL = "jdbc:mysql://localhost:3306/banking_simulator";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static boolean initialized = false; // ✅ prevents duplicate messages

    public DatabaseService() {
        if (!initialized) {  // only create once
            createTablesIfNotExists();
            initialized = true;
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void createTablesIfNotExists() {
        String createAccounts = """
            CREATE TABLE IF NOT EXISTS accounts (
                id INT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                balance DOUBLE NOT NULL
            );
        """;

        String createTransactions = """
            CREATE TABLE IF NOT EXISTS transactions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                type VARCHAR(50) NOT NULL,
                from_acc INT,
                to_acc INT,
                amount DOUBLE NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                note VARCHAR(255),
                FOREIGN KEY (from_acc) REFERENCES accounts(id) ON DELETE SET NULL ON UPDATE CASCADE,
                FOREIGN KEY (to_acc) REFERENCES accounts(id) ON DELETE SET NULL ON UPDATE CASCADE
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createAccounts);
            stmt.execute(createTransactions);

            // 🔇 Silent success — remove console print
            // System.out.println("✅ Database and tables are ready!");

        } catch (SQLException e) {
            System.err.println("❌ Error initializing database: " + e.getMessage());
        }
    }
}
