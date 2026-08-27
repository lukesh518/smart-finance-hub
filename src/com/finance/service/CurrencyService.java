package com.finance.service;

import com.finance.repository.FileRepository;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service Layer for Multi-Currency Conversion & User Preference Persistence (Feature 7).
 */
public class CurrencyService {
    private String selectedCurrency; // USD, EUR, GBP, INR, JPY
    private final Map<String, Double> exchangeRates; // Rates relative to 1 USD
    private final Map<String, String> currencySymbols;

    public CurrencyService() {
        this.selectedCurrency = FileRepository.loadUserCurrency();
        this.exchangeRates = new HashMap<>();
        this.currencySymbols = new HashMap<>();

        // Base Exchange Rates relative to 1 USD
        exchangeRates.put("USD", 1.0);
        exchangeRates.put("EUR", 0.92);
        exchangeRates.put("GBP", 0.79);
        exchangeRates.put("INR", 83.50);
        exchangeRates.put("JPY", 155.00);

        // Symbols
        currencySymbols.put("USD", "$");
        currencySymbols.put("EUR", "€");
        currencySymbols.put("GBP", "£");
        currencySymbols.put("INR", "₹");
        currencySymbols.put("JPY", "¥");
    }

    public String getSelectedCurrency() {
        return selectedCurrency;
    }

    public String getSymbol() {
        return currencySymbols.getOrDefault(selectedCurrency, "$");
    }

    public double getRate() {
        return exchangeRates.getOrDefault(selectedCurrency, 1.0);
    }

    public void setCurrency(String currency) throws IOException {
        if (exchangeRates.containsKey(currency.toUpperCase())) {
            this.selectedCurrency = currency.toUpperCase();
            FileRepository.saveUserCurrency(selectedCurrency);
        }
    }

    public double convert(double usdAmount) {
        return usdAmount * getRate();
    }

    public String format(double usdAmount) {
        double converted = convert(usdAmount);
        String symbol = getSymbol();
        if ("JPY".equals(selectedCurrency)) {
            return String.format("%s%.0f", symbol, converted);
        }
        return String.format("%s%.2f", symbol, converted);
    }
}
