package com.bank.simulator.model;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Account {
    private static final Set<Long> USED_IDS = new HashSet<>();
    private final long accountId;
    private String ownerName;
    private String email;
    private String dob;       // YYYY-MM-DD format
    private String location;
    private double balance;
    private double alertThreshold = 100.0;

    // 🔹 Constructor for creating new account
    public Account(String ownerName, String email, String dob, String location, double initialBalance) {
        this.accountId = generateUniqueId();
        this.ownerName = ownerName;
        this.email = email;
        this.dob = dob;
        this.location = location;
        this.balance = initialBalance;
    }

    // 🔹 Constructor for loading from database
    public Account(long accountId, String ownerName, String email, String dob, String location, double balance) {
        this.accountId = accountId;
        this.ownerName = ownerName;
        this.email = email;
        this.dob = dob;
        this.location = location;
        this.balance = balance;
    }

    // 🔹 Generate unique 4-digit ID
    private static long generateUniqueId() {
        Random random = new Random();
        long id;
        do {
            id = 1000 + random.nextInt(9000);
        } while (USED_IDS.contains(id));
        USED_IDS.add(id);
        return id;
    }

    // 🔹 Getters
    public long getAccountId() { return accountId; }
    public String getOwnerName() { return ownerName; }
    public String getEmail() { return email; }
    public String getDob() { return dob; }
    public String getLocation() { return location; }
    public double getBalance() { return balance; }
    public double getAlertThreshold() { return alertThreshold; }

    // 🔹 Setters
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setEmail(String email) { this.email = email; }
    public void setDob(String dob) { this.dob = dob; }
    public void setLocation(String location) { this.location = location; }
    public void setAlertThreshold(double alertThreshold) { this.alertThreshold = alertThreshold; }

    // 🔹 Update balance
    public void deposit(double amount) {
        if (amount > 0) balance += amount;
    }

    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) balance -= amount;
    }

    public void setBalance(double balance) { this.balance = balance; } // for AccountManager DB updates

    // 🔹 Display
    @Override
    public String toString() {
        return "Account{ID=" + accountId +
                ", Name='" + ownerName + '\'' +
                ", Email='" + email + '\'' +
                ", DOB='" + dob + '\'' +
                ", Location='" + location + '\'' +
                ", Balance=" + balance + "}";
    }
}
