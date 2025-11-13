package com.bank.simulator;

import com.bank.simulator.model.Account;
import com.bank.simulator.service.*;
import com.bank.simulator.exception.*;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 🔹 Initialize services
        DatabaseService dbService = new DatabaseService();
        AccountManager accountManager = new AccountManager(dbService);
        ReportingService reportingService = new ReportingService();
        AlertService alertService = new AlertService();
        TransactionProcessor transactionProcessor =
                new TransactionProcessor(accountManager, alertService, reportingService);

        System.out.println("💰 Welcome to Banking Simulator 💰");

        while (true) {
            System.out.println("\n1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Show All Accounts");
            System.out.println("6. Generate Report");
            System.out.println("7. Exit");
            System.out.println("8. Check Account Balance");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            try {
                switch (choice) {
                    // ✅ Account Creation with validation
                case 1:
                    System.out.print("Owner name: ");
                    String name = sc.nextLine().trim();
                    System.out.print("Email address: ");
                    String email = sc.nextLine().trim();

                    // ✅ Validate date of birth
                    String dob;
                    while (true) {
                        System.out.print("Date of Birth (YYYY-MM-DD): ");
                        dob = sc.nextLine().trim();
                        try {
                            java.time.LocalDate.parse(dob); // validate format + valid date
                            break;
                        } catch (Exception e) {
                            System.out.println("⚠️ Invalid date! Please enter a valid date in YYYY-MM-DD format.");
                        }
                    }

                    System.out.print("Location: ");
                    String location = sc.nextLine().trim();

                    double balance;
                    while (true) {
                        System.out.print("Initial balance: ");
                        String input = sc.nextLine().trim();
                        try {
                            balance = Double.parseDouble(input);
                            if (balance <= 0) {
                                System.out.println("⚠️ Invalid amount! Please enter a value greater than 0.");
                                continue;
                            }
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("⚠️ Invalid amount, please enter a valid positive number.");
                        }
                    }

                    try {
                        Account acc = accountManager.createAccount(name, email, dob, location, balance);
                        System.out.println("✅ Account created: " + acc);
                    } catch (Exception e) {
                        System.err.println("❌ Error creating account: " + e.getMessage());
                    }
                    break;

                    // ✅ Deposit validation
                    case 2:
                        System.out.print("Account ID: ");
                        long depId = sc.nextLong();
                        sc.nextLine();
                        double depAmount;
                        while (true) {
                            System.out.print("Deposit amount: ");
                            String input = sc.nextLine().trim();
                            try {
                                depAmount = Double.parseDouble(input);
                                if (depAmount <= 0) {
                                    System.out.println("⚠️ Invalid amount! Deposit must be greater than 0.");
                                    continue;
                                }
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("⚠️ Invalid amount, please enter a valid number.");
                            }
                        }
                        transactionProcessor.deposit(depId, depAmount, "Deposit");
                        break;

                    // ✅ Withdraw validation
                    case 3:
                        System.out.print("Account ID: ");
                        long withId = sc.nextLong();
                        sc.nextLine();
                        double withAmount;
                        while (true) {
                            System.out.print("Withdrawal amount: ");
                            String input = sc.nextLine().trim();
                            try {
                                withAmount = Double.parseDouble(input);
                                if (withAmount <= 0) {
                                    System.out.println("⚠️ Invalid amount! Withdrawal must be greater than 0.");
                                    continue;
                                }
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("⚠️ Invalid amount, please enter a valid number.");
                            }
                        }
                        transactionProcessor.withdraw(withId, withAmount, "Withdraw");
                        break;

                    // ✅ Transfer
                    case 4:
                        System.out.print("Sender Account ID: ");
                        long fromId = sc.nextLong();
                        System.out.print("Receiver Account ID: ");
                        long toId = sc.nextLong();
                        System.out.print("Transfer amount: ");
                        double transAmount = sc.nextDouble();
                        sc.nextLine();
                        if (transAmount <= 0) {
                            System.out.println("⚠️ Invalid amount! Transfer must be greater than 0.");
                        } else {
                            transactionProcessor.transfer(fromId, toId, transAmount, "Transfer");
                        }
                        break;

                    // ✅ Show All Accounts
                    case 5:
                        accountManager.listAllAccountsFromDB();
                        break;

                    // ✅ Generate Report
                    case 6:
                        reportingService.generateReport();
                        break;

                    // ✅ Exit
                    case 7:
                        System.out.println("🙏 Thank you for using the Banking Simulator!");
                        sc.close();
                        System.exit(0);
                        break;

                    // ✅ Check Account Balance
                    case 8:
                        System.out.print("Account ID: ");
                        long checkId = sc.nextLong();
                        sc.nextLine();
                        Account checkAcc = accountManager.getAccount(checkId);
                        System.out.println("💰 Balance for account " + checkId + ": ₹" + checkAcc.getBalance());
                        break;

                    default:
                        System.out.println("⚠️ Invalid choice! Please try again.");
                        break;
                }

            } catch (AccountNotFoundException | InvalidAmountException | InsufficientFundsException e) {
                System.out.println("❌ Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("❌ Unexpected error: " + e.getMessage());
            }
        }
    }
}
