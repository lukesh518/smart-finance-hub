package com.finance.model;

/**
 * Enum for Investment Portfolio Risk Levels.
 */
public enum PortfolioRisk {
    LOW("Low Risk (Conservative)", "High stability, lower volatility (e.g. BTC, ETH, Large Cap)"),
    MEDIUM("Moderate Risk (Balanced)", "Balanced growth & market fluctuations (e.g. SOL, ADA, Tech Stocks)"),
    HIGH("High Risk (Aggressive)", "High volatility & speculative assets (e.g. Altcoins, Meme Tokens)");

    private final String title;
    private final String description;

    PortfolioRisk(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
