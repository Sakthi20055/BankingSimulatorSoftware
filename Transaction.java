package com.bank.simulator.model;

import java.time.LocalDateTime;

public class Transaction {
    private TransactionType type;
    private long fromAccountId;
    private long toAccountId;
    private double amount;
    private LocalDateTime timestamp;
    private String note;

    public Transaction(TransactionType type, long fromAccountId, long toAccountId, double amount, String note) {
        this.type = type;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.note = note;
        this.timestamp = LocalDateTime.now();
    }

    public TransactionType getType() { return type; }
    public long getFromAccountId() { return fromAccountId; }
    public long getToAccountId() { return toAccountId; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getNote() { return note; }
}

