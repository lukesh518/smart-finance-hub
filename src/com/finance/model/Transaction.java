package com.finance.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Model representing income and expense transactions.
 */
public class Transaction {
    private final String id;
    private String description;
    private double amount;
    private LocalDate date;
    private Category category;
    private boolean isIncome;

    public Transaction(String description, double amount, LocalDate date, Category category, boolean isIncome) {
        this.id = "TXN-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.description = description != null ? description.trim() : "Uncategorized";
        this.amount = Math.abs(amount);
        this.date = date != null ? date : LocalDate.now();
        this.category = category != null ? category : Category.OTHER;
        this.isIncome = isIncome;
    }

    public Transaction(String id, String description, double amount, LocalDate date, Category category, boolean isIncome) {
        this.id = id;
        this.description = description != null ? description.trim() : "Uncategorized";
        this.amount = Math.abs(amount);
        this.date = date != null ? date : LocalDate.now();
        this.category = category != null ? category : Category.OTHER;
        this.isIncome = isIncome;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public Category getCategory() { return category; }
    public boolean isIncome() { return isIncome; }

    public String toCsvRow() {
        return String.join(",",
                id,
                "\"" + description.replace("\"", "\"\"") + "\"",
                String.valueOf(amount),
                date.toString(),
                category.name(),
                String.valueOf(isIncome)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] ID: %s | %s | %-22s | $%.2f | %s",
                isIncome ? "INCOME" : "EXPENSE", id, date, description, amount, category.getDisplayName());
    }
}
