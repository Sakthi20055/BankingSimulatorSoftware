package com.bank.simulator.service;

import com.bank.simulator.exception.AccountNotFoundException;
import com.bank.simulator.model.Account;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AccountManager {
    private final DatabaseService db;
    private final Map<Long, Account> accounts = new HashMap<>(); // runtime cache

    public AccountManager(DatabaseService db) {
        this.db = db;
    }

    // 🔹 Create new account and log initial deposit
    public Account createAccount(String ownerName, String email, String dob, String location, double initialBalance) {
        Account acc = new Account(ownerName, email, dob, location, initialBalance);

        String sql = "INSERT INTO accounts (account_id, owner_name, email, dob, location, balance) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, acc.getAccountId());
            ps.setString(2, ownerName);
            ps.setString(3, email);
            ps.setString(4, dob);
            ps.setString(5, location);
            ps.setDouble(6, initialBalance);
            ps.executeUpdate();

            // ✅ Add to runtime cache
            accounts.put(acc.getAccountId(), acc);

            // ✅ Log initial deposit into transactions table
            logInitialDeposit(acc);

            System.out.println("✅ Account created successfully!");
            return acc;

        } catch (SQLException e) {
            throw new RuntimeException("❌ Error creating account: " + e.getMessage());
        }
    }

    // 🔹 Log the initial deposit in the transactions table
    private void logInitialDeposit(Account acc) {
        String txnSql = "INSERT INTO transactions (type, from_acc, amount, note) VALUES (?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(txnSql)) {

            ps.setString(1, "Initial Deposit");
            ps.setLong(2, acc.getAccountId());
            ps.setDouble(3, acc.getBalance());
            ps.setString(4, "Initial deposit during account creation");
            ps.executeUpdate();

            System.out.println("🧾 Initial deposit of ₹" + acc.getBalance() +
                    " logged for account ID " + acc.getAccountId());

        } catch (SQLException e) {
            System.err.println("⚠️ Failed to log initial deposit: " + e.getMessage());
        }
    }

    // 🔹 Get account by ID (first check cache, then DB)
    public Account getAccount(long accountId) throws AccountNotFoundException {
        if (accounts.containsKey(accountId)) return accounts.get(accountId);

        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Account acc = new Account(
                        rs.getLong("account_id"),
                        rs.getString("owner_name"),
                        rs.getString("email"),
                        rs.getString("dob"),
                        rs.getString("location"),
                        rs.getDouble("balance")
                );
                accounts.put(acc.getAccountId(), acc);
                return acc;
            } else {
                throw new AccountNotFoundException("⚠️ Account not found with ID: " + accountId);
            }

        } catch (SQLException e) {
            throw new RuntimeException("❌ Error fetching account: " + e.getMessage());
        }
    }

    // 🔹 Update balance (for deposits, withdrawals, or transfers)
    public void updateBalance(long accountId, double newBalance) throws AccountNotFoundException {
        Account acc = getAccount(accountId);
        acc.setBalance(newBalance);

        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newBalance);
            ps.setLong(2, accountId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("❌ Error updating balance: " + e.getMessage());
        }
    }

    // 🔹 Return all accounts currently cached in memory
    public Collection<Account> getAllAccounts() {
        return accounts.values();
    }

    // 🔹 List all accounts from database
    public void listAllAccountsFromDB() {
        String sql = "SELECT * FROM accounts";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean hasData = false;
            System.out.println("\n📜 All Accounts:");
            while (rs.next()) {
                hasData = true;
                long id = rs.getLong("account_id");
                String name = rs.getString("owner_name");
                String email = rs.getString("email");
                String dob = rs.getString("dob");
                String location = rs.getString("location");
                double balance = rs.getDouble("balance");

                System.out.println("Account{ID=" + id +
                        ", Name='" + name + '\'' +
                        ", Email='" + email + '\'' +
                        ", DOB='" + dob + '\'' +
                        ", Location='" + location + '\'' +
                        ", Balance=" + balance + "}");
            }

            if (!hasData) {
                System.out.println("⚠️ No accounts found in the database.");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching accounts: " + e.getMessage());
        }
    }
}
