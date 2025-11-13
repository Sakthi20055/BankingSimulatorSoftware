package com.bank.simulator.service;

import com.bank.simulator.exception.*;
import com.bank.simulator.model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class TransactionProcessor {

    private final AccountManager accountManager;
    private final AlertService alertService;
    private final ReportingService reportingService;
    private final DatabaseService db;

    public TransactionProcessor(AccountManager accountManager,
                                AlertService alertService,
                                ReportingService reportingService) {
        this.accountManager = accountManager;
        this.alertService = alertService;
        this.reportingService = reportingService;
        this.db = new DatabaseService();
    }

    // ========================= DEPOSIT =========================
    public void deposit(long accountId, double amount, String note)
            throws AccountNotFoundException, InvalidAmountException {

        Account account = accountManager.getAccount(accountId);
        if (account == null) throw new AccountNotFoundException("Account not found: " + accountId);

        if (amount <= 0) throw new InvalidAmountException("Deposit amount must be positive.");

        double newBalance = account.getBalance() + amount;
        account.setBalance(newBalance);
        accountManager.updateBalance(accountId, newBalance);

        reportingService.logTransaction("Deposit", accountId, amount, note);
        recordTransaction("Deposit", null, accountId, amount, note);

        System.out.println("✅ Deposit successful!");
        System.out.println("💰 Updated Balance: ₹" + newBalance);
    }

    // ========================= WITHDRAWAL WITH OTP =========================
    public void withdraw(long accountId, double amount, String note)
            throws AccountNotFoundException, InvalidAmountException, InsufficientFundsException {

        Account account = accountManager.getAccount(accountId);
        if (account == null) throw new AccountNotFoundException("Account not found: " + accountId);

        if (amount <= 0) throw new InvalidAmountException("Withdrawal amount must be positive.");
        if (amount > account.getBalance()) throw new InsufficientFundsException("Insufficient funds.");

        // 🔑 Step 1: Generate OTP and send via email
        String generatedOtp = alertService.sendOtp(account);

        // 🔑 Step 2: Ask user to enter OTP
        System.out.print("Enter OTP sent to your email: ");
        Scanner scanner = new Scanner(System.in);
        String enteredOtp = scanner.nextLine();

        if (!generatedOtp.equals(enteredOtp)) {
            System.out.println("❌ Invalid OTP. Withdrawal cancelled.");
            return; // Stop the withdrawal
        }

        // 🔑 Step 3: Proceed with withdrawal
        double newBalance = account.getBalance() - amount;
        account.setBalance(newBalance);
        accountManager.updateBalance(accountId, newBalance);

        reportingService.logTransaction("Withdraw", accountId, amount, note);
        recordTransaction("Withdraw", accountId, null, amount, note);

        System.out.println("✅ Withdrawal successful!");
        System.out.println("💰 Remaining Balance: ₹" + newBalance);

        // ⚠️ Send low balance alert if below threshold
        if (newBalance < account.getAlertThreshold()) {
            alertService.sendLowBalanceAlert(account);
        }
    }

    // ========================= TRANSFER =========================
    public void transfer(long fromAccountId, long toAccountId, double amount, String note)
            throws AccountNotFoundException, InvalidAmountException, InsufficientFundsException {

        Account fromAccount = accountManager.getAccount(fromAccountId);
        Account toAccount = accountManager.getAccount(toAccountId);

        if (fromAccount == null) throw new AccountNotFoundException("Sender account not found.");
        if (toAccount == null) throw new AccountNotFoundException("Receiver account not found.");
        if (amount <= 0) throw new InvalidAmountException("Transfer amount must be positive.");
        if (amount > fromAccount.getBalance()) throw new InsufficientFundsException("Sender has insufficient funds.");

        double fromNewBalance = fromAccount.getBalance() - amount;
        double toNewBalance = toAccount.getBalance() + amount;

        fromAccount.setBalance(fromNewBalance);
        toAccount.setBalance(toNewBalance);

        accountManager.updateBalance(fromAccountId, fromNewBalance);
        accountManager.updateBalance(toAccountId, toNewBalance);

        reportingService.logTransaction("Transfer", fromAccountId, amount, note);
        recordTransaction("Transfer", fromAccountId, toAccountId, amount, note);

        System.out.println("✅ Transfer successful!");
        System.out.println("💸 Amount: ₹" + amount);
        System.out.println("🏦 From Account " + fromAccountId + " → To Account " + toAccountId);
        System.out.println("💰 Sender Remaining Balance: ₹" + fromNewBalance);
        System.out.println("💰 Receiver New Balance: ₹" + toNewBalance);

        // ⚠️ Low balance alert for sender
        if (fromNewBalance < fromAccount.getAlertThreshold()) {
            alertService.sendLowBalanceAlert(fromAccount);
        }
    }

    // ========================= RECORD TRANSACTION IN DB =========================
    private void recordTransaction(String type, Long fromId, Long toId, double amount, String note) {
        String sql = "INSERT INTO transactions (type, from_acc, to_acc, amount, note) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, type);
            ps.setObject(2, fromId); // safely handles null
            ps.setObject(3, toId);   // safely handles null
            ps.setDouble(4, amount);
            ps.setString(5, note);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Error recording transaction: " + e.getMessage());
        }
    }
}
