package com.bank.simulator.service;

import java.sql.*;

public class ReportingService {

    private final DatabaseService db;

    public ReportingService() {
        this.db = new DatabaseService();
    }

    // ✅ Log a single transaction (Deposit or Withdraw) with optional note
    public void logTransaction(String type, long accountId, double amount, String note) {
        String sql = "INSERT INTO transactions (type, from_acc, amount, note) VALUES (?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type);
            ps.setLong(2, accountId);
            ps.setDouble(3, amount);
            ps.setString(4, note != null ? note : type + " transaction completed successfully");
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Error logging transaction: " + e.getMessage());
        }
    }

    // ✅ Log a transfer transaction with optional note
    public void logTransfer(String type, long fromAcc, long toAcc, double amount, String note) {
        String sql = "INSERT INTO transactions (type, from_acc, to_acc, amount, note) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type);
            ps.setLong(2, fromAcc);
            ps.setLong(3, toAcc);
            ps.setDouble(4, amount);
            ps.setString(5, note != null ? note : "Transfer transaction completed successfully");
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Error logging transfer: " + e.getMessage());
        }
    }

    // ✅ Generate a detailed transaction report from DB
    public void generateReport() {
        String sql = """
                SELECT 
                    id, type, from_acc, to_acc, amount, timestamp, note
                FROM 
                    transactions
                ORDER BY timestamp DESC
                """;

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n📊 Transaction History Report");
            System.out.println("------------------------------------------------------------");
            System.out.printf("%-5s %-12s %-10s %-10s %-10s %-20s %-20s\n",
                    "ID", "TYPE", "FROM_ACC", "TO_ACC", "AMOUNT", "TIMESTAMP", "NOTE");
            System.out.println("------------------------------------------------------------");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int id = rs.getInt("id");
                String type = rs.getString("type");
                long fromAcc = rs.getLong("from_acc");
                long toAcc = rs.getLong("to_acc");
                double amount = rs.getDouble("amount");
                Timestamp time = rs.getTimestamp("timestamp");
                String note = rs.getString("note");

                System.out.printf("%-5d %-12s %-10s %-10s %-10.2f %-20s %-20s\n",
                        id,
                        type,
                        (fromAcc == 0 ? "-" : String.valueOf(fromAcc)),
                        (toAcc == 0 ? "-" : String.valueOf(toAcc)),
                        amount,
                        time.toString(),
                        note);
            }

            if (!hasData) {
                System.out.println("⚠️ No transactions found yet.");
            }

            System.out.println("------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("❌ Error generating report: " + e.getMessage());
        }
    }
}
