package com.finance.exceptions;

/**
 * Exception thrown when adding an expense breaches defined budget limits or triggers overspending warnings.
 */
public class BudgetBreachException extends Exception {
    private final double currentExpense;
    private final double budgetLimit;

    public BudgetBreachException(String message, double currentExpense, double budgetLimit) {
        super(message);
        this.currentExpense = currentExpense;
        this.budgetLimit = budgetLimit;
    }

    public double getCurrentExpense() { return currentExpense; }
    public double getBudgetLimit() { return budgetLimit; }
}
