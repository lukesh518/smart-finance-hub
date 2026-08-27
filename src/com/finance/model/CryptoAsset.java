package com.finance.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Model representing cryptocurrency and stock holdings.
 */
public class CryptoAsset {
    private final String id;
    private String symbol;      // e.g. BTC, ETH, SOL, AAPL
    private String assetName;   // e.g. Bitcoin, Ethereum, Solana
    private double quantity;
    private double buyPrice;
    private double currentPrice;
    private PortfolioRisk riskLevel;

    public CryptoAsset(String symbol, String assetName, double quantity, double buyPrice, double currentPrice, PortfolioRisk riskLevel) {
        this.id = "AST-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.symbol = symbol != null ? symbol.toUpperCase().trim() : "UNKNOWN";
        this.assetName = assetName != null ? assetName.trim() : symbol;
        this.quantity = Math.max(0, quantity);
        this.buyPrice = Math.max(0, buyPrice);
        this.currentPrice = Math.max(0, currentPrice);
        this.riskLevel = riskLevel != null ? riskLevel : PortfolioRisk.MEDIUM;
    }

    public CryptoAsset(String id, String symbol, String assetName, double quantity, double buyPrice, double currentPrice, PortfolioRisk riskLevel) {
        this.id = id;
        this.symbol = symbol != null ? symbol.toUpperCase().trim() : "UNKNOWN";
        this.assetName = assetName != null ? assetName.trim() : symbol;
        this.quantity = Math.max(0, quantity);
        this.buyPrice = Math.max(0, buyPrice);
        this.currentPrice = Math.max(0, currentPrice);
        this.riskLevel = riskLevel != null ? riskLevel : PortfolioRisk.MEDIUM;
    }

    public String getId() { return id; }
    public String getSymbol() { return symbol; }
    public String getAssetName() { return assetName; }
    public double getQuantity() { return quantity; }
    public double getBuyPrice() { return buyPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public PortfolioRisk getRiskLevel() { return riskLevel; }

    public double getTotalInvestmentCost() { return quantity * buyPrice; }
    public double getCurrentMarketValue() { return quantity * currentPrice; }
    public double getProfitLoss() { return getCurrentMarketValue() - getTotalInvestmentCost(); }
    public double getProfitLossPercentage() {
        double cost = getTotalInvestmentCost();
        return cost > 0 ? (getProfitLoss() / cost) * 100.0 : 0.0;
    }

    public String toCsvRow() {
        return String.join(",",
                id,
                symbol,
                "\"" + assetName.replace("\"", "\"\"") + "\"",
                String.valueOf(quantity),
                String.valueOf(buyPrice),
                String.valueOf(currentPrice),
                riskLevel.name()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CryptoAsset)) return false;
        CryptoAsset that = (CryptoAsset) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("[%s] ID: %s | %-15s (%s) | Qty: %.4f | Buy: $%.2f | Cur: $%.2f | P/L: %+.2f (%.1f%%)",
                riskLevel.name(), id, assetName, symbol, quantity, buyPrice, currentPrice, getProfitLoss(), getProfitLossPercentage());
    }
}
