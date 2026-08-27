package com.finance.repository;

import com.finance.model.*;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository layer for CSV file persistence across all 7 features.
 */
public class FileRepository {
    private static final String TXN_FILE = "transactions.csv";
    private static final String SUB_FILE = "subscriptions.csv";
    private static final String CRYPTO_FILE = "investments.csv";
    private static final String CONFIG_FILE = "config.txt";
    private static final String USER_SETTINGS_FILE = "user_settings.txt";
    private static final String WATCHLIST_FILE = "user_watchlist.csv";
    private static final String CHAT_FILE = "chat_history.csv";
    private static final String APPS_FILE = "investment_apps.csv";

    // 1. Transactions I/O
    public static void saveTransactions(List<Transaction> list) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TXN_FILE))) {
            writer.write("id,description,amount,date,category,isIncome\n");
            for (Transaction t : list) {
                writer.write(t.toCsvRow());
                writer.newLine();
            }
        }
    }

    public static List<Transaction> loadTransactions() {
        List<Transaction> list = new ArrayList<>();
        File file = new File(TXN_FILE);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.trim().isEmpty()) continue;
                List<String> t = parseCsvLine(line);
                if (t.size() >= 6) {
                    list.add(new Transaction(t.get(0), t.get(1), Double.parseDouble(t.get(2)),
                            LocalDate.parse(t.get(3)), Category.valueOf(t.get(4)), Boolean.parseBoolean(t.get(5))));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading transactions: " + e.getMessage());
        }
        return list;
    }

    // 2. Subscriptions I/O
    public static void saveSubscriptions(List<Subscription> list) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SUB_FILE))) {
            writer.write("id,serviceName,monthlyCost,nextBillingDate,billingCycle,autoRenew\n");
            for (Subscription s : list) {
                writer.write(s.toCsvRow());
                writer.newLine();
            }
        }
    }

    public static List<Subscription> loadSubscriptions() {
        List<Subscription> list = new ArrayList<>();
        File file = new File(SUB_FILE);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.trim().isEmpty()) continue;
                List<String> t = parseCsvLine(line);
                if (t.size() >= 6) {
                    list.add(new Subscription(t.get(0), t.get(1), Double.parseDouble(t.get(2)),
                            LocalDate.parse(t.get(3)), t.get(4), Boolean.parseBoolean(t.get(5))));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading subscriptions: " + e.getMessage());
        }
        return list;
    }

    // 3. Crypto / Investments I/O
    public static void saveCryptoAssets(List<CryptoAsset> list) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CRYPTO_FILE))) {
            writer.write("id,symbol,assetName,quantity,buyPrice,currentPrice,riskLevel\n");
            for (CryptoAsset a : list) {
                writer.write(a.toCsvRow());
                writer.newLine();
            }
        }
    }

    public static List<CryptoAsset> loadCryptoAssets() {
        List<CryptoAsset> list = new ArrayList<>();
        File file = new File(CRYPTO_FILE);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.trim().isEmpty()) continue;
                List<String> t = parseCsvLine(line);
                if (t.size() >= 7) {
                    list.add(new CryptoAsset(t.get(0), t.get(1), t.get(2), Double.parseDouble(t.get(3)),
                            Double.parseDouble(t.get(4)), Double.parseDouble(t.get(5)), PortfolioRisk.valueOf(t.get(6))));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading investments: " + e.getMessage());
        }
        return list;
    }

    // 4. Budget Settings I/O
    public static void saveBudget(double budget) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
            writer.write("monthly_budget=" + budget);
        }
    }

    public static double loadBudget() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return 1500.0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null && line.startsWith("monthly_budget=")) {
                return Double.parseDouble(line.substring(15));
            }
        } catch (Exception e) {
            System.err.println("Error loading config: " + e.getMessage());
        }
        return 1500.0;
    }

    // 5. Feature 7: Multi-Currency Settings I/O
    public static void saveUserCurrency(String currency) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_SETTINGS_FILE))) {
            writer.write("currency=" + currency.toUpperCase());
        }
    }

    public static String loadUserCurrency() {
        File file = new File(USER_SETTINGS_FILE);
        if (!file.exists()) return "USD";
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null && line.startsWith("currency=")) {
                return line.substring(9).trim();
            }
        } catch (Exception e) {
            System.err.println("Error loading user currency: " + e.getMessage());
        }
        return "USD";
    }

    // 6. Feature 4: User Watchlist I/O
    public static void saveCryptoWatchlist(List<String> symbols) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(WATCHLIST_FILE))) {
            for (String s : symbols) {
                writer.write(s.toUpperCase().trim());
                writer.newLine();
            }
        }
    }

    public static List<String> loadCryptoWatchlist() {
        List<String> list = new ArrayList<>();
        File file = new File(WATCHLIST_FILE);
        if (!file.exists()) return list;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) list.add(line.trim().toUpperCase());
            }
        } catch (Exception e) {
            System.err.println("Error loading watchlist: " + e.getMessage());
        }
        return list;
    }

    // 7. Feature 2: Chat History I/O
    public static void saveChatHistory(List<String[]> history) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CHAT_FILE))) {
            writer.write("role,message\n");
            for (String[] entry : history) {
                writer.write(entry[0] + ",\"" + entry[1].replace("\"", "\"\"") + "\"\n");
            }
        }
    }

    public static List<String[]> loadChatHistory() {
        List<String[]> list = new ArrayList<>();
        File file = new File(CHAT_FILE);
        if (!file.exists()) return list;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.trim().isEmpty()) continue;
                List<String> t = parseCsvLine(line);
                if (t.size() >= 2) {
                    list.add(new String[]{t.get(0), t.get(1)});
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading chat history: " + e.getMessage());
        }
        return list;
    }

    // 8. Feature 6: Investment App Recommendations I/O
    public static List<String[]> loadInvestmentApps() {
        List<String[]> list = new ArrayList<>();
        File file = new File(APPS_FILE);
        if (!file.exists()) {
            initDefaultInvestmentApps();
            file = new File(APPS_FILE);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.trim().isEmpty()) continue;
                List<String> t = parseCsvLine(line);
                if (t.size() >= 5) {
                    list.add(new String[]{t.get(0), t.get(1), t.get(2), t.get(3), t.get(4)});
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading investment apps: " + e.getMessage());
        }
        return list;
    }

    private static void initDefaultInvestmentApps() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(APPS_FILE))) {
            writer.write("id,appName,logoUrl,keyFeatures,whyThisApp\n");
            writer.write("APP-1,\"Vanguard\",\"📈\",\"Low-cost Index Funds & ETFs | IRA Accounts\",\"Best for long-term passive investors & retirement planning.\"\n");
            writer.write("APP-2,\"Robinhood\",\"📱\",\"Commission-free Stocks, Options & Crypto | Sleek UI\",\"Best for beginners & instant mobile trading.\"\n");
            writer.write("APP-3,\"Coinbase\",\"⚡\",\"Top Security Crypto Exchange | Staking Rewards\",\"Best for safe cryptocurrency buying, selling & staking.\"\n");
            writer.write("APP-4,\"Wealthfront\",\"🤖\",\"Automated Robo-Advisor | Daily Tax-Loss Harvesting\",\"Best for hands-off automated portfolio management.\"\n");
            writer.write("APP-5,\"M1 Finance\",\"🥧\",\"Custom Pie Portals | Auto-Rebalancing\",\"Best for visual pie-chart portfolio building & dividend reinvestment.\"\n");
        } catch (IOException e) {
            System.err.println("Error initializing default investment apps: " + e.getMessage());
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens;
    }
}
