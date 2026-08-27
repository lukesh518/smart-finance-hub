package com.finance.model;

/**
 * Category classification with Priority distinction (Important/Essential vs Non-Essential).
 */
public enum Category {
    STUDY("Study & Education (Tuition, Books, Courses)", false, true),
    ESSENTIAL("Essential Utility & Health", false, true),
    HOUSING("Housing & Rent", false, true),
    FOOD_DINING("Dining Out & Fast Food", false, false),
    ENTERTAINMENT("Entertainment & Movies", false, false),
    TRAVEL("Leisure Travel & Shopping", false, false),
    SALARY("Salary & Main Income", true, false),
    FREELANCE("Freelance & Side Income", true, false),
    OTHER("Other Miscellaneous", false, false);

    private final String displayName;
    private final boolean isIncome;
    private final boolean isImportant; // true = Essential/Study, false = Non-essential/Discretionary

    Category(String displayName, boolean isIncome, boolean isImportant) {
        this.displayName = displayName;
        this.isIncome = isIncome;
        this.isImportant = isImportant;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public boolean isImportant() {
        return isImportant;
    }

    @Override
    public String toString() {
        return displayName + (isImportant ? " [ESSENTIAL]" : " [DISCRETIONARY]");
    }
}
