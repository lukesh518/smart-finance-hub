package com.finance.service;

import com.finance.repository.FileRepository;
import java.io.IOException;
import java.util.*;

/**
 * Service Layer for Personalized Crypto Watchlist & Hourly Trend Data (Feature 4).
 * Uses in-memory server-side caching to prevent API rate limits.
 */
public class CryptoWatchlistService {
    private final List<String> watchlistSymbols; // e.g. ["BTC", "ETH", "SOL", "ADA"]
    private final Map<String, List<Double>> cachedPriceHistory; // Symbol -> Hourly prices
    private long lastFetchTimestamp;
    private static final long CACHE_DURATION_MS = 60 * 60 * 1000; // 60 minutes in-memory cache

    public CryptoWatchlistService() {
        this.watchlistSymbols = FileRepository.loadCryptoWatchlist();
        this.cachedPriceHistory = new HashMap<>();
        this.lastFetchTimestamp = 0;
        initMockDataIfEmpty();
    }

    public List<String> getWatchlist() {
        return Collections.unmodifiableList(watchlistSymbols);
    }

    public void addSymbol(String symbol) throws IOException {
        String clean = symbol.toUpperCase().trim();
        if (!clean.isEmpty() && !watchlistSymbols.contains(clean)) {
            watchlistSymbols.add(clean);
            FileRepository.saveCryptoWatchlist(watchlistSymbols);
            generateMockHistoryForSymbol(clean);
        }
    }

    public void removeSymbol(String symbol) throws IOException {
        boolean removed = watchlistSymbols.removeIf(s -> s.equalsIgnoreCase(symbol.trim()));
        if (removed) {
            FileRepository.saveCryptoWatchlist(watchlistSymbols);
        }
    }

    public Map<String, List<Double>> getHourlyPriceTrends() {
        long now = System.currentTimeMillis();
        if (cachedPriceHistory.isEmpty() || (now - lastFetchTimestamp) > CACHE_DURATION_MS) {
            refreshPriceHistory();
            lastFetchTimestamp = now;
        }
        return cachedPriceHistory;
    }

    private void refreshPriceHistory() {
        for (String symbol : watchlistSymbols) {
            generateMockHistoryForSymbol(symbol);
        }
    }

    private void generateMockHistoryForSymbol(String symbol) {
        double basePrice = switch (symbol.toUpperCase()) {
            case "BTC" -> 64500.0;
            case "ETH" -> 3450.0;
            case "SOL" -> 155.0;
            case "ADA" -> 0.48;
            case "DOGE" -> 0.14;
            case "AVAX" -> 32.5;
            default -> 100.0;
        };

        List<Double> hourlyPrices = new ArrayList<>();
        Random rand = new Random(symbol.hashCode());
        double current = basePrice * 0.95;
        for (int i = 0; i < 24; i++) { // 24 hours trend
            double changePct = (rand.nextDouble() - 0.48) * 0.03; // ~3% volatility
            current = current * (1.0 + changePct);
            hourlyPrices.add(Math.round(current * 100.0) / 100.0);
        }
        cachedPriceHistory.put(symbol.toUpperCase(), hourlyPrices);
    }

    private void initMockDataIfEmpty() {
        if (watchlistSymbols.isEmpty()) {
            watchlistSymbols.add("BTC");
            watchlistSymbols.add("ETH");
            watchlistSymbols.add("SOL");
            watchlistSymbols.add("ADA");
            try {
                FileRepository.saveCryptoWatchlist(watchlistSymbols);
            } catch (IOException e) {
                System.err.println("Warning saving initial watchlist: " + e.getMessage());
            }
        }
        refreshPriceHistory();
    }
}
