package com.finance.service;

import com.finance.exceptions.InvalidAssetException;
import com.finance.model.CryptoAsset;
import com.finance.model.PortfolioRisk;
import com.finance.repository.FileRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Service Layer for Crypto/Stock Holdings, Growth Simulator & Investment Advisory Engine.
 */
public class CryptoService {
    private final List<CryptoAsset> assets;

    public CryptoService() {
        this.assets = FileRepository.loadCryptoAssets();
    }

    public void addAsset(CryptoAsset asset) throws InvalidAssetException, IOException {
        if (asset.getQuantity() <= 0) {
            throw new InvalidAssetException("Quantity must be greater than zero.");
        }
        assets.add(asset);
        FileRepository.saveCryptoAssets(assets);
    }

    public boolean removeAsset(String id) throws IOException {
        boolean removed = assets.removeIf(a -> a.getId().equalsIgnoreCase(id));
        if (removed) FileRepository.saveCryptoAssets(assets);
        return removed;
    }

    public List<CryptoAsset> getAllAssets() {
        return Collections.unmodifiableList(assets);
    }

    public double calculateTotalPortfolioValue() {
        return assets.stream().mapToDouble(CryptoAsset::getCurrentMarketValue).sum();
    }

    public double calculateTotalInvestmentCost() {
        return assets.stream().mapToDouble(CryptoAsset::getTotalInvestmentCost).sum();
    }

    public double calculateTotalProfitLoss() {
        return calculateTotalPortfolioValue() - calculateTotalInvestmentCost();
    }

    public PortfolioRisk evaluateOverallPortfolioRisk() {
        if (assets.isEmpty()) return PortfolioRisk.LOW;

        long highRiskCount = assets.stream().filter(a -> a.getRiskLevel() == PortfolioRisk.HIGH).count();
        long medRiskCount = assets.stream().filter(a -> a.getRiskLevel() == PortfolioRisk.MEDIUM).count();

        double highRiskRatio = (double) highRiskCount / assets.size();
        if (highRiskRatio >= 0.4) return PortfolioRisk.HIGH;
        if (medRiskCount + highRiskCount >= assets.size() / 2) return PortfolioRisk.MEDIUM;

        return PortfolioRisk.LOW;
    }

    /**
     * Simulates future compound growth for recurring monthly savings invested over N years.
     * @param monthlyInvestment Monthly savings injected into portfolio
     * @param years Duration in years (e.g. 1, 3, 5)
     * @param annualReturnRate Estimated annual return percentage (e.g. 12.0 for 12% per year)
     * @return Estimated total future value
     */
    public double calculateProjectedGrowth(double monthlyInvestment, int years, double annualReturnRate) {
        int months = years * 12;
        double monthlyRate = (annualReturnRate / 100.0) / 12.0;
        double futureValue = 0.0;

        for (int i = 0; i < months; i++) {
            futureValue = (futureValue + monthlyInvestment) * (1.0 + monthlyRate);
        }
        return futureValue;
    }
}
