package com.finance.service;

import com.finance.exceptions.BudgetBreachException;
import com.finance.exceptions.InvalidAssetException;
import com.finance.model.*;
import com.finance.repository.FileRepository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Layer for Expense Tracking, Dual-Priority Classification & Edit Operations.
 */
public class ExpenseService {
    private final List<Transaction> transactions;
    private double monthlyBudget;

    public ExpenseService() {
        this.transactions = FileRepository.loadTransactions();
        this.monthlyBudget = FileRepository.loadBudget();
    }

    public void addTransaction(Transaction txn, boolean forceAdd) throws BudgetBreachException, InvalidAssetException, IOException {
        if (txn.getAmount() <= 0) {
            throw new InvalidAssetException("Amount must be greater than zero.");
        }

        if (!txn.isIncome() && monthlyBudget > 0) {
            double currentExpense = getTotalExpenses();
            double projected = currentExpense + txn.getAmount();

            if (projected > monthlyBudget && !forceAdd) {
                throw new BudgetBreachException(
                        "Expense of $" + String.format("%.2f", txn.getAmount()) +
                        " breaches monthly budget limit of $" + String.format("%.2f", monthlyBudget) +
                        "! Current Expenses: $" + String.format("%.2f", currentExpense),
                        currentExpense, monthlyBudget
                );
            }
        }

        transactions.add(txn);
        FileRepository.saveTransactions(transactions);
    }

    public boolean editTransaction(String id, String newDesc, double newAmount, Category newCategory, boolean newIsIncome) throws InvalidAssetException, IOException {
        if (newAmount <= 0) {
            throw new InvalidAssetException("Amount must be greater than zero.");
        }
        for (Transaction t : transactions) {
            if (t.getId().equalsIgnoreCase(id)) {
                // Update transaction attributes
                transactions.remove(t);
                Transaction updated = new Transaction(id, newDesc, newAmount, t.getDate(), newCategory, newIsIncome);
                transactions.add(updated);
                FileRepository.saveTransactions(transactions);
                return true;
            }
        }
        return false;
    }

    public boolean deleteTransaction(String id) throws IOException {
        boolean removed = transactions.removeIf(t -> t.getId().equalsIgnoreCase(id));
        if (removed) FileRepository.saveTransactions(transactions);
        return removed;
    }

    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public double getTotalIncome() {
        return transactions.stream().filter(Transaction::isIncome).mapToDouble(Transaction::getAmount).sum();
    }

    public double getTotalExpenses() {
        return transactions.stream().filter(t -> !t.isIncome()).mapToDouble(Transaction::getAmount).sum();
    }

    public double getImportantExpensesTotal() {
        return transactions.stream()
                .filter(t -> !t.isIncome() && t.getCategory().isImportant())
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getNonEssentialExpensesTotal() {
        return transactions.stream()
                .filter(t -> !t.isIncome() && !t.getCategory().isImportant())
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getNetSavings() {
        return getTotalIncome() - getTotalExpenses();
    }

    public double getRecommendedMonthlySavings() {
        double nonEssential = getNonEssentialExpensesTotal();
        double potentialCut = nonEssential * 0.40;
        return getNetSavings() + potentialCut;
    }

    public double getMonthlyBudget() { return monthlyBudget; }

    public void setMonthlyBudget(double budget) throws IOException {
        this.monthlyBudget = Math.max(0, budget);
        FileRepository.saveBudget(monthlyBudget);
    }

    public Map<Category, Double> getExpensesByCategory() {
        Map<Category, Double> map = new EnumMap<>(Category.class);
        for (Transaction t : transactions) {
            if (!t.isIncome()) {
                map.put(t.getCategory(), map.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
            }
        }
        return map;
    }
}
