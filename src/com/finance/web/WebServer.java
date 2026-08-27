package com.finance.web;

import com.finance.model.*;
import com.finance.repository.FileRepository;
import com.finance.service.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/**
 * Web Server supporting Full Transaction Editing (Description & Amount).
 */
public class WebServer {
    private final ExpenseService expenseService;
    private final SubscriptionService subscriptionService;
    private final CryptoService cryptoService;
    private final CurrencyService currencyService;
    private final CryptoWatchlistService watchlistService;
    private final AiAdvisorService aiAdvisorService;
    private final int port;

    public WebServer(ExpenseService expenseService, SubscriptionService subscriptionService,
                     CryptoService cryptoService, CurrencyService currencyService,
                     CryptoWatchlistService watchlistService, AiAdvisorService aiAdvisorService, int port) {
        this.expenseService = expenseService;
        this.subscriptionService = subscriptionService;
        this.cryptoService = cryptoService;
        this.currencyService = currencyService;
        this.watchlistService = watchlistService;
        this.aiAdvisorService = aiAdvisorService;
        this.port = port;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new DashboardHandler());
        server.createContext("/api/add-transaction", new AddTransactionHandler());
        server.createContext("/api/edit-transaction", new EditTransactionHandler());
        server.createContext("/api/delete-transaction", new DeleteTransactionHandler());
        server.createContext("/api/set-budget", new SetBudgetHandler());
        server.createContext("/api/add-subscription", new AddSubscriptionHandler());
        server.createContext("/api/delete-subscription", new DeleteSubscriptionHandler());
        server.createContext("/api/add-asset", new AddAssetHandler());
        server.createContext("/api/delete-asset", new DeleteAssetHandler());
        server.createContext("/api/chat", new ChatHandler());
        server.createContext("/api/add-watchlist", new AddWatchlistHandler());
        server.createContext("/api/remove-watchlist", new RemoveWatchlistHandler());
        server.createContext("/api/set-currency", new SetCurrencyHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("==================================================================");
        System.out.println("🚀 SMART FINANCE WEB APP ONLINE AT http://localhost:" + port);
        System.out.println("==================================================================");
    }

    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = generatePageHtml();
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // Transaction Action Handlers
    private class AddTransactionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    String desc = params.getOrDefault("desc", "Web Entry");
                    double amount = Double.parseDouble(params.getOrDefault("amount", "0"));
                    Category cat = Category.valueOf(params.getOrDefault("category", "OTHER"));
                    boolean isIncome = Boolean.parseBoolean(params.getOrDefault("isIncome", "false"));

                    Transaction txn = new Transaction(desc, amount, LocalDate.now(), cat, isIncome);
                    expenseService.addTransaction(txn, true);
                } catch (Exception e) {
                    System.err.println("Error adding transaction: " + e.getMessage());
                }
            }
            redirectHome(exchange);
        }
    }

    private class EditTransactionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    String id = params.getOrDefault("id", "");
                    String desc = params.getOrDefault("desc", "Updated");
                    double amount = Double.parseDouble(params.getOrDefault("amount", "0"));
                    Category cat = Category.valueOf(params.getOrDefault("category", "OTHER"));
                    boolean isIncome = Boolean.parseBoolean(params.getOrDefault("isIncome", "false"));

                    if (!id.isEmpty()) {
                        expenseService.editTransaction(id, desc, amount, cat, isIncome);
                    }
                } catch (Exception e) {
                    System.err.println("Error editing transaction: " + e.getMessage());
                }
            }
            redirectHome(exchange);
        }
    }

    private class DeleteTransactionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                String id = params.getOrDefault("id", "");
                if (!id.isEmpty()) {
                    expenseService.deleteTransaction(id);
                }
            }
            redirectHome(exchange);
        }
    }

    private class SetBudgetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    double budget = Double.parseDouble(params.getOrDefault("budget", "1500"));
                    expenseService.setMonthlyBudget(budget);
                } catch (Exception e) {
                    System.err.println("Error setting budget: " + e.getMessage());
                }
            }
            redirectHome(exchange);
        }
    }

    // Subscription Handlers
    private class AddSubscriptionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    String name = params.getOrDefault("name", "Service");
                    double cost = Double.parseDouble(params.getOrDefault("cost", "0"));
                    int days = Integer.parseInt(params.getOrDefault("days", "30"));
                    Subscription sub = new Subscription(name, cost, LocalDate.now().plusDays(days), "MONTHLY", true);
                    subscriptionService.addSubscription(sub);
                } catch (Exception e) {
                    System.err.println("Error adding sub: " + e.getMessage());
                }
            }
            redirectHome(exchange);
        }
    }

    private class DeleteSubscriptionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                String id = params.getOrDefault("id", "");
                if (!id.isEmpty()) {
                    subscriptionService.removeSubscription(id);
                }
            }
            redirectHome(exchange);
        }
    }

    // Crypto Asset Handlers
    private class AddAssetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    String symbol = params.getOrDefault("symbol", "BTC");
                    String name = params.getOrDefault("name", "Bitcoin");
                    double qty = Double.parseDouble(params.getOrDefault("qty", "0"));
                    double buy = Double.parseDouble(params.getOrDefault("buyPrice", "0"));
                    double cur = Double.parseDouble(params.getOrDefault("curPrice", "0"));
                    PortfolioRisk risk = PortfolioRisk.valueOf(params.getOrDefault("risk", "MEDIUM"));

                    CryptoAsset asset = new CryptoAsset(symbol, name, qty, buy, cur, risk);
                    cryptoService.addAsset(asset);
                } catch (Exception e) {
                    System.err.println("Error adding asset: " + e.getMessage());
                }
            }
            redirectHome(exchange);
        }
    }

    private class DeleteAssetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                String id = params.getOrDefault("id", "");
                if (!id.isEmpty()) {
                    cryptoService.removeAsset(id);
                }
            }
            redirectHome(exchange);
        }
    }

    private class ChatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                String msg = params.getOrDefault("message", "");
                if (!msg.trim().isEmpty()) {
                    aiAdvisorService.generateResponse(msg.trim());
                }
            }
            redirectHome(exchange);
        }
    }

    private class AddWatchlistHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                String symbol = params.getOrDefault("symbol", "");
                if (!symbol.trim().isEmpty()) {
                    watchlistService.addSymbol(symbol);
                }
            }
            redirectHome(exchange);
        }
    }

    private class RemoveWatchlistHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                String symbol = params.getOrDefault("symbol", "");
                if (!symbol.trim().isEmpty()) {
                    watchlistService.removeSymbol(symbol);
                }
            }
            redirectHome(exchange);
        }
    }

    private class SetCurrencyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                String curr = params.getOrDefault("currency", "USD");
                currencyService.setCurrency(curr);
            }
            redirectHome(exchange);
        }
    }

    private Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String query = br.readLine();
        Map<String, String> map = new HashMap<>();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] s = pair.split("=");
                if (s.length == 2) {
                    map.put(URLDecoder.decode(s[0], StandardCharsets.UTF_8), URLDecoder.decode(s[1], StandardCharsets.UTF_8));
                }
            }
        }
        return map;
    }

    private void redirectHome(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Location", "/");
        exchange.sendResponseHeaders(302, -1);
    }

    private String generatePageHtml() {
        List<Transaction> txns = expenseService.getAllTransactions();
        List<Subscription> subs = subscriptionService.getAllSubscriptions();
        List<CryptoAsset> assets = cryptoService.getAllAssets();
        List<String[]> appRecs = FileRepository.loadInvestmentApps();
        List<String[]> leaks = aiAdvisorService.analyzeNonEssentialLeaks(txns);
        Map<String, List<Double>> trends = watchlistService.getHourlyPriceTrends();

        double essentialExp = expenseService.getImportantExpensesTotal();
        double nonEssentialExp = expenseService.getNonEssentialExpensesTotal();
        double totalExp = expenseService.getTotalExpenses();
        double totalInc = expenseService.getTotalIncome();
        double currentSavings = expenseService.getNetSavings();
        double budget = expenseService.getMonthlyBudget();
        double potentialSavings = expenseService.getRecommendedMonthlySavings();
        double potentialCut = nonEssentialExp * 0.40;

        double essentialPct = totalExp > 0 ? (essentialExp / totalExp) * 100 : 0;
        double nonEssentialPct = totalExp > 0 ? (nonEssentialExp / totalExp) * 100 : 0;

        // 1. Transaction Table Rows with EDIT and DELETE Options
        StringBuilder txnRows = new StringBuilder();
        for (Transaction t : txns) {
            String badgeClass = t.isIncome() ? "badge-income" : (t.getCategory().isImportant() ? "badge-essential" : "badge-nonessential");
            String badgeText = t.isIncome() ? "INCOME" : (t.getCategory().isImportant() ? "🎓 ESSENTIAL" : "✈️ LIFESTYLE");
            txnRows.append(String.format("""
                <tr>
                    <td><code class="code-tag">%s</code></td>
                    <td><span class="badge %s">%s</span></td>
                    <td><strong>%s</strong></td>
                    <td class="%s"><strong>%s</strong></td>
                    <td>%s</td>
                    <td>%s</td>
                    <td style="display:flex; gap:6px;">
                        <button type="button" class="btn-edit" onclick="openEditModal('%s', '%s', '%.2f', '%s', '%b')">✏️ Edit</button>
                        <form action="/api/delete-transaction" method="POST" style="margin:0;">
                            <input type="hidden" name="id" value="%s">
                            <button type="submit" class="btn-delete">🗑️ Delete</button>
                        </form>
                    </td>
                </tr>""",
                t.getId(), badgeClass, badgeText, t.getDescription(),
                t.isIncome() ? "text-green" : "text-red", currencyService.format(t.getAmount()),
                t.getCategory().getDisplayName(), t.getDate(),
                t.getId(), t.getDescription().replace("'", "\\'"), t.getAmount(), t.getCategory().name(), t.isIncome(),
                t.getId()
            ));
        }

        // 2. Subscription Rows
        StringBuilder subRows = new StringBuilder();
        for (Subscription s : subs) {
            String badge = s.isUrgentRenewal() ? "<span class='badge badge-urgent'>⚠️ DUE SOON</span>" : "<span class='badge badge-ok'>ACTIVE</span>";
            subRows.append(String.format("""
                <tr>
                    <td><code class="code-tag">%s</code></td>
                    <td><strong>%s</strong></td>
                    <td>%s/mo</td>
                    <td>%s</td>
                    <td><span class="pill-tag">%d Days Left</span></td>
                    <td>%s</td>
                    <td>
                        <form action="/api/delete-subscription" method="POST" style="margin:0;">
                            <input type="hidden" name="id" value="%s">
                            <button type="submit" class="btn-delete">❌ Cancel Sub</button>
                        </form>
                    </td>
                </tr>""",
                s.getId(), s.getServiceName(), currencyService.format(s.getMonthlyCost()), s.getNextBillingDate(), s.getDaysUntilRenewal(), badge, s.getId()
            ));
        }

        // 3. Crypto Asset Rows
        StringBuilder cryptoRows = new StringBuilder();
        for (CryptoAsset a : assets) {
            String plClass = a.getProfitLoss() >= 0 ? "text-green" : "text-red";
            cryptoRows.append(String.format("""
                <tr>
                    <td><code class="code-tag">%s</code></td>
                    <td><strong>%s</strong> <span style="color:var(--subtext);">(%s)</span></td>
                    <td>%.4f</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td><strong>%s</strong></td>
                    <td class="%s"><strong>%s (%.1f%%)</strong></td>
                    <td><span class="badge badge-risk-%s">%s</span></td>
                    <td>
                        <form action="/api/delete-asset" method="POST" style="margin:0;">
                            <input type="hidden" name="id" value="%s">
                            <button type="submit" class="btn-delete">🗑️ Sell / Remove</button>
                        </form>
                    </td>
                </tr>""",
                a.getId(), a.getAssetName(), a.getSymbol(), a.getQuantity(),
                currencyService.format(a.getBuyPrice()), currencyService.format(a.getCurrentPrice()),
                currencyService.format(a.getCurrentMarketValue()), plClass,
                (a.getProfitLoss() >= 0 ? "+" : "") + currencyService.format(a.getProfitLoss()), a.getProfitLossPercentage(),
                a.getRiskLevel().name().toLowerCase(), a.getRiskLevel().name(), a.getId()
            ));
        }

        // Watchlist Cards
        StringBuilder watchlistHtml = new StringBuilder();
        for (String sym : watchlistService.getWatchlist()) {
            List<Double> pList = trends.getOrDefault(sym, Arrays.asList(100.0, 105.0, 102.0, 108.0));
            double latest = pList.isEmpty() ? 0.0 : pList.get(pList.size() - 1);
            double first = pList.isEmpty() ? 1.0 : pList.get(0);
            double changePct = first > 0 ? ((latest - first) / first) * 100.0 : 0.0;
            String changeClass = changePct >= 0 ? "text-green" : "text-red";

            StringBuilder svgPoints = new StringBuilder();
            if (pList.size() > 1) {
                double min = Collections.min(pList);
                double max = Collections.max(pList);
                double range = max - min > 0 ? max - min : 1.0;
                for (int i = 0; i < pList.size(); i++) {
                    double x = (i / (double) (pList.size() - 1)) * 180.0;
                    double y = 45.0 - ((pList.get(i) - min) / range) * 35.0;
                    svgPoints.append(String.format(Locale.US, "%.1f,%.1f ", x, y));
                }
            }

            watchlistHtml.append(String.format("""
                <div class="watch-card">
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <strong>%s / USD</strong>
                        <form action="/api/remove-watchlist" method="POST" style="margin:0;">
                            <input type="hidden" name="symbol" value="%s">
                            <button type="submit" style="background:none; border:none; color:var(--red); cursor:pointer; font-size:0.8rem;">✕ Remove</button>
                        </form>
                    </div>
                    <div style="font-size:1.3rem; font-weight:800; margin:6px 0;">%s <span class="%s" style="font-size:0.8rem;">%+.1f%%</span></div>
                    <svg width="180" height="45" style="overflow:visible;">
                        <polyline fill="none" stroke="%s" stroke-width="2" points="%s" />
                    </svg>
                </div>""",
                sym, sym, currencyService.format(latest), changeClass, changePct,
                changePct >= 0 ? "#10b981" : "#f43f5e", svgPoints.toString().trim()
            ));
        }

        // Leaks Cards
        StringBuilder leakCards = new StringBuilder();
        for (String[] leak : leaks) {
            leakCards.append(String.format("""
                <div class="leak-card">
                    <div style="display:flex; justify-content:space-between;">
                        <span class="badge badge-nonessential">#%s Money Leak: %s</span>
                        <strong class="text-green">%s Savings</strong>
                    </div>
                    <p style="font-size:0.85rem; margin:10px 0; color:#cbd5e1;">%s</p>
                    <span style="font-size:0.75rem; color:var(--subtext);">Recent Spent: %s</span>
                </div>""",
                leak[0], leak[1], leak[4], leak[3], leak[2]
            ));
        }

        // Apps Cards
        StringBuilder appCards = new StringBuilder();
        for (String[] app : appRecs) {
            appCards.append(String.format("""
                <div class="app-card">
                    <div style="font-size:2rem; margin-bottom:5px;">%s</div>
                    <h4 style="margin:5px 0; font-size:1.1rem; color:var(--primary);">%s</h4>
                    <p style="font-size:0.8rem; color:var(--subtext); margin-bottom:10px;">%s</p>
                    <div style="background:rgba(255,255,255,0.03); padding:8px; border-radius:8px; font-size:0.75rem; color:#cbd5e1; border:1px solid var(--card-border);">
                        💡 <strong>Why this app:</strong> %s
                    </div>
                </div>""",
                app[2], app[1], app[3], app[4]
            ));
        }

        // Chat History
        StringBuilder chatHtml = new StringBuilder();
        for (String[] msg : aiAdvisorService.getChatHistory()) {
            boolean isUser = "user".equalsIgnoreCase(msg[0]);
            chatHtml.append(String.format("""
                <div class="chat-msg %s">
                    <div class="chat-bubble">%s</div>
                </div>""",
                isUser ? "user-msg" : "bot-msg", msg[1]
            ));
        }

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Personal Finance Tracking Platform</title>
                <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
                <style>
                    :root {
                        --bg: #090d16; --card-bg: rgba(21, 29, 46, 0.8); --card-border: rgba(255, 255, 255, 0.08);
                        --primary: #38bdf8; --primary-glow: rgba(56, 189, 248, 0.25);
                        --green: #10b981; --red: #f43f5e; --purple: #c084fc; --gold: #fbbf24; --text: #f8fafc; --subtext: #94a3b8;
                    }
                    body.light-mode {
                        --bg: #f8fafc; --card-bg: #ffffff; --card-border: rgba(0,0,0,0.1);
                        --text: #0f172a; --subtext: #64748b;
                    }
                    * { box-sizing: border-box; transition: all 0.2s ease-in-out; }
                    body { font-family: 'Plus Jakarta Sans', sans-serif; background: var(--bg); color: var(--text); margin: 0; padding: 0; min-height: 100vh; }
                    
                    .navbar { display: flex; justify-content: space-between; align-items: center; padding: 16px 40px; background: rgba(15, 23, 42, 0.9); backdrop-filter: blur(16px); border-bottom: 1px solid var(--card-border); position: sticky; top: 0; z-index: 100; }
                    .logo { font-size: 1.4rem; font-weight: 800; background: linear-gradient(135deg, #38bdf8, #c084fc); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
                    .nav-tabs { display: flex; gap: 10px; background: rgba(255, 255, 255, 0.05); padding: 5px; border-radius: 12px; }
                    .tab-btn { background: transparent; border: none; color: var(--subtext); padding: 8px 18px; font-weight: 700; font-size: 0.85rem; border-radius: 8px; cursor: pointer; }
                    .tab-btn.active, .tab-btn:hover { background: var(--primary); color: #000; box-shadow: 0 0 15px var(--primary-glow); }
                    
                    .nav-right { display: flex; align-items: center; gap: 12px; }

                    .content-container { max-width: 1300px; margin: 30px auto; padding: 0 30px; }
                    
                    .page-section { display: none; }
                    .page-section.active-page { display: block; }

                    .grid-4 { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 30px; }
                    .stat-card { background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 20px; padding: 22px; }
                    .stat-card h4 { margin: 0 0 8px 0; font-size: 0.8rem; text-transform: uppercase; color: var(--subtext); }
                    .stat-card .val { font-size: 1.8rem; font-weight: 800; margin: 0; }

                    .charts-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 30px; }
                    .chart-card { background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 20px; padding: 25px; }

                    .watch-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 15px; margin-top: 15px; }
                    .watch-card { background: rgba(255,255,255,0.03); border: 1px solid var(--card-border); border-radius: 16px; padding: 15px; }

                    .leak-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 15px; margin-top: 15px; }
                    .leak-card { background: rgba(255,255,255,0.03); border: 1px solid var(--card-border); border-radius: 16px; padding: 18px; }

                    .app-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 20px; margin-top: 15px; }
                    .app-card { background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 20px; padding: 20px; }

                    .ai-section { background: linear-gradient(135deg, rgba(30, 27, 75, 0.8), rgba(49, 16, 75, 0.8)); border: 1px solid rgba(192, 132, 252, 0.3); border-radius: 24px; padding: 30px; margin-bottom: 35px; box-shadow: 0 20px 40px rgba(0,0,0,0.4); }
                    .proj-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 20px; margin-top: 20px; }
                    .proj-card { background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08); border-radius: 16px; padding: 20px; text-align: center; }

                    .chat-bubble-btn { position: fixed; bottom: 30px; right: 30px; width: 60px; height: 60px; border-radius: 50%%; background: linear-gradient(135deg, #38bdf8, #c084fc); border: none; font-size: 1.5rem; cursor: pointer; box-shadow: 0 10px 25px var(--primary-glow); z-index: 999; }
                    .chat-window { display: none; position: fixed; bottom: 100px; right: 30px; width: 380px; height: 480px; background: #1e293b; border: 1px solid var(--card-border); border-radius: 20px; flex-direction: column; overflow: hidden; box-shadow: 0 20px 50px rgba(0,0,0,0.5); z-index: 999; }
                    .chat-header { background: #0f172a; padding: 15px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--card-border); }
                    .chat-messages { flex: 1; padding: 15px; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; }
                    .chat-msg { display: flex; }
                    .user-msg { justify-content: flex-end; }
                    .bot-msg { justify-content: flex-start; }
                    .chat-bubble { max-width: 80%%; padding: 10px 14px; border-radius: 14px; font-size: 0.85rem; line-height: 1.4; }
                    .user-msg .chat-bubble { background: var(--primary); color: #000; font-weight: 600; }
                    .bot-msg .chat-bubble { background: rgba(255,255,255,0.08); color: var(--text); border: 1px solid var(--card-border); }

                    .table-card { background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 20px; padding: 25px; margin-bottom: 30px; }
                    table { width: 100%%; border-collapse: collapse; }
                    th { text-align: left; padding: 12px; font-size: 0.8rem; text-transform: uppercase; color: var(--subtext); border-bottom: 1px solid var(--card-border); }
                    td { padding: 12px; font-size: 0.85rem; border-bottom: 1px solid var(--card-border); }

                    .code-tag { background: rgba(255,255,255,0.06); padding: 3px 8px; border-radius: 6px; font-family: monospace; font-size: 0.8rem; color: var(--primary); }
                    .pill-tag { background: rgba(255,255,255,0.05); padding: 3px 10px; border-radius: 20px; font-size: 0.75rem; color: var(--subtext); }

                    .badge { padding: 4px 10px; border-radius: 20px; font-size: 0.75rem; font-weight: 700; display: inline-block; }
                    .badge-income { background: rgba(16, 185, 129, 0.2); color: var(--green); border: 1px solid var(--green); }
                    .badge-essential { background: rgba(56, 189, 248, 0.15); color: var(--primary); border: 1px solid var(--primary); }
                    .badge-nonessential { background: rgba(244, 63, 94, 0.15); color: var(--red); border: 1px solid var(--red); }
                    .badge-urgent { background: rgba(244, 63, 94, 0.2); color: var(--red); border: 1px solid var(--red); }
                    .badge-ok { background: rgba(16, 185, 129, 0.2); color: var(--green); }
                    .badge-risk-low { background: rgba(16, 185, 129, 0.2); color: var(--green); }
                    .badge-risk-medium { background: rgba(251, 191, 36, 0.2); color: var(--gold); }
                    .badge-risk-high { background: rgba(244, 63, 94, 0.2); color: var(--red); }

                    .btn-action { background: linear-gradient(135deg, #38bdf8, #818cf8); border: none; color: #000; padding: 8px 16px; border-radius: 10px; font-weight: 700; font-size: 0.85rem; cursor: pointer; }
                    .btn-edit { background: rgba(56, 189, 248, 0.15); border: 1px solid var(--primary); color: var(--primary); padding: 5px 10px; border-radius: 6px; font-size: 0.75rem; cursor: pointer; }
                    .btn-edit:hover { background: var(--primary); color: #000; }
                    .btn-delete { background: rgba(244, 63, 94, 0.15); border: 1px solid var(--red); color: var(--red); padding: 5px 10px; border-radius: 6px; font-size: 0.75rem; cursor: pointer; }
                    .btn-delete:hover { background: var(--red); color: #fff; }

                    .modal { display: none; position: fixed; top:0; left:0; width:100%%; height:100%%; background: rgba(0,0,0,0.7); backdrop-filter: blur(8px); z-index: 1000; justify-content: center; align-items: center; }
                    .modal-content { background: #1e293b; border: 1px solid var(--card-border); border-radius: 20px; width: 450px; padding: 30px; }
                    .form-group { margin-bottom: 15px; }
                    .form-group label { display: block; font-size: 0.85rem; color: var(--subtext); margin-bottom: 6px; }
                    .form-group input, .form-group select { width: 100%%; padding: 10px; border-radius: 8px; background: #0f172a; border: 1px solid #334155; color: #fff; font-size: 0.9rem; }

                    .text-green { color: var(--green); } .text-red { color: var(--red); }
                </style>
            </head>
            <body>
                <!-- Navbar -->
                <div class="navbar">
                    <div class="logo">📊 Personal Finance Tracking Platform</div>
                    <div class="nav-tabs">
                        <button id="btn-finance" class="tab-btn active" onclick="showPage('finance')">📊 Finance Tracking</button>
                        <button id="btn-subscriptions" class="tab-btn" onclick="showPage('subscriptions')">🔄 Subscriptions</button>
                        <button id="btn-crypto" class="tab-btn" onclick="showPage('crypto')">⚡ Crypto & Investment</button>
                        <button id="btn-ai" class="tab-btn" onclick="showPage('ai')">🤖 AI Advisor & Tips</button>
                    </div>
                    <div class="nav-right">
                        <form action="/api/set-currency" method="POST" style="margin:0;">
                            <select name="currency" onchange="this.form.submit()" style="background:#1e293b; color:#fff; border:1px solid var(--card-border); padding:6px 12px; border-radius:8px; font-size:0.85rem;">
                                <option value="USD" %s>USD ($)</option>
                                <option value="EUR" %s>EUR (€)</option>
                                <option value="GBP" %s>GBP (£)</option>
                                <option value="INR" %s>INR (₹)</option>
                                <option value="JPY" %s>JPY (¥)</option>
                            </select>
                        </form>
                        <button onclick="toggleTheme()" style="background:rgba(255,255,255,0.08); border:1px solid var(--card-border); color:var(--text); padding:6px 14px; border-radius:8px; cursor:pointer; font-size:0.85rem;">🌓 Theme</button>
                    </div>
                </div>

                <div class="content-container">

                    <!-- PAGE 1: PERSONAL FINANCE TRACKING -->
                    <div id="page-finance" class="page-section active-page">
                        <div class="grid-4">
                            <div class="stat-card">
                                <h4>Net Savings Capacity</h4>
                                <p class="val text-green">%s</p>
                                <span style="font-size:0.75rem; color:var(--subtext);">Total Income: %s</span>
                            </div>
                            <div class="stat-card">
                                <h4>🎓 Essential Study & Living</h4>
                                <p class="val text-blue">%s</p>
                                <span style="font-size:0.75rem; color:var(--subtext);">Tuition, Books, Rent</span>
                            </div>
                            <div class="stat-card">
                                <h4>✈️ Non-Essential Lifestyle</h4>
                                <p class="val text-red">%s</p>
                                <span style="font-size:0.75rem; color:var(--subtext);">Dining Out, Travel</span>
                            </div>
                            <div class="stat-card">
                                <h4>Monthly Budget Limit</h4>
                                <p class="val text-green">%s</p>
                                <span style="font-size:0.75rem; color:var(--subtext);">Defined Limit</span>
                            </div>
                        </div>

                        <div class="charts-grid">
                            <div class="chart-card">
                                <h4 style="margin-top:0; color:var(--primary);">📊 Monthly Cash Flow Graph</h4>
                                <svg width="100%%" height="180" viewBox="0 0 350 180">
                                    <rect x="40" y="%f" width="50" height="%f" fill="#10b981" rx="6"/>
                                    <text x="65" y="165" fill="var(--subtext)" font-size="11" text-anchor="middle">Income</text>
                                    
                                    <rect x="150" y="%f" width="50" height="%f" fill="#f43f5e" rx="6"/>
                                    <text x="175" y="165" fill="var(--subtext)" font-size="11" text-anchor="middle">Expense</text>
                                    
                                    <rect x="260" y="%f" width="50" height="%f" fill="#38bdf8" rx="6"/>
                                    <text x="285" y="165" fill="var(--subtext)" font-size="11" text-anchor="middle">Savings</text>
                                </svg>
                            </div>

                            <div class="chart-card">
                                <h4 style="margin-top:0; color:var(--primary);">🥧 Category Expense Breakdown</h4>
                                <div style="display:flex; align-items:center; justify-content:space-around;">
                                    <svg width="140" height="140" viewBox="0 0 32 32" style="transform: rotate(-90deg); border-radius:50%%;">
                                        <circle r="16" cx="16" cy="16" fill="transparent" stroke="#38bdf8" stroke-width="32" stroke-dasharray="%f 100" />
                                        <circle r="16" cx="16" cy="16" fill="transparent" stroke="#f43f5e" stroke-width="32" stroke-dasharray="%f 100" stroke-dashoffset="-%f" />
                                    </svg>
                                    <div style="font-size:0.8rem; line-height:1.8;">
                                        <div><span style="color:var(--primary);">■</span> Essential Study/Rent (%s)</div>
                                        <div><span style="color:var(--red);">■</span> Non-Essential Lifestyle (%s)</div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Table with Edit & Delete Actions -->
                        <div class="table-card">
                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
                                <h3 style="margin:0; color:var(--primary);">📊 Expense & Income Ledger</h3>
                                <div style="display:flex; gap:10px;">
                                    <button class="btn-action" onclick="openModal('modal-txn')">➕ Log Expense / Income</button>
                                    <button class="btn-action" style="background:#475569; color:#fff;" onclick="openModal('modal-budget')">⚙️ Change Budget</button>
                                </div>
                            </div>
                            <table>
                                <thead>
                                    <tr><th>ID</th><th>Priority Class</th><th>Description</th><th>Amount</th><th>Category</th><th>Date</th><th>Action</th></tr>
                                </thead>
                                <tbody>%s</tbody>
                            </table>
                        </div>
                    </div>

                    <!-- PAGE 2: SUBSCRIPTIONS MANAGER -->
                    <div id="page-subscriptions" class="page-section">
                        <div class="table-card">
                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
                                <h3 style="margin:0; color:var(--primary);">🔄 Subscriptions & Recurring Bills Sentinel</h3>
                                <button class="btn-action" onclick="openModal('modal-sub')">➕ Add New Subscription</button>
                            </div>
                            <p style="color:var(--subtext); font-size:0.9rem; margin-bottom:15px;">
                                Total Monthly Cost: <strong>%s</strong> | Urgent Renewals: <strong>%d Service(s)</strong>
                            </p>
                            <table>
                                <thead>
                                    <tr><th>ID</th><th>Service Name</th><th>Monthly Cost</th><th>Next Billing Date</th><th>Days Left</th><th>Alert Status</th><th>Action</th></tr>
                                </thead>
                                <tbody>%s</tbody>
                            </table>
                        </div>
                    </div>

                    <!-- PAGE 3: CRYPTO & INVESTMENT HUB -->
                    <div id="page-crypto" class="page-section">
                        <div class="table-card">
                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:15px;">
                                <h3 style="margin:0; color:var(--primary);">📈 Live Crypto Watchlist & Hourly Trends</h3>
                                <form action="/api/add-watchlist" method="POST" style="display:flex; gap:8px; margin:0;">
                                    <input type="text" name="symbol" placeholder="Coin Symbol (e.g. DOGE)" required style="background:#0f172a; color:#fff; border:1px solid var(--card-border); padding:6px; border-radius:6px; font-size:0.8rem;">
                                    <button type="submit" class="btn-action">+ Watch</button>
                                </form>
                            </div>
                            <div class="watch-grid">%s</div>
                        </div>

                        <div class="table-card">
                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
                                <h3 style="margin:0; color:var(--primary);">⚡ Crypto & Stock Portfolio Holdings</h3>
                                <button class="btn-action" onclick="openModal('modal-asset')">⚡ Buy / Add Asset</button>
                            </div>
                            <p style="color:var(--subtext); font-size:0.9rem; margin-bottom:15px;">
                                Total Portfolio Value: <strong>%s</strong> | Overall Risk: <strong>%s</strong>
                            </p>
                            <table>
                                <thead>
                                    <tr><th>ID</th><th>Asset Name</th><th>Quantity</th><th>Buy Price</th><th>Current Price</th><th>Market Value</th><th>Profit / Loss</th><th>Risk Level</th><th>Action</th></tr>
                                </thead>
                                <tbody>%s</tbody>
                            </table>
                        </div>
                    </div>

                    <!-- PAGE 4: AI ADVISOR & ANALYTICS -->
                    <div id="page-ai" class="page-section">
                        <div class="ai-section">
                            <h2>🤖 AI Wealth Advisor & Compound Growth Simulator</h2>
                            <p style="color: #cbd5e1; font-size: 0.95rem; line-height: 1.6;">
                                💡 <strong>Smart Recommendation:</strong> By curbing 40%% of non-essential lifestyle spending (dining out, travel), you unlock an extra <strong>+%s/month</strong>, elevating monthly investment capacity to <strong style="color:var(--gold);">%s/month</strong>.
                            </p>

                            <div class="proj-cards">
                                <div class="proj-card">
                                    <div style="font-size:0.85rem; color:var(--subtext);">📅 1-Year Projected Portfolio</div>
                                    <div style="font-size:1.7rem; font-weight:800; color:var(--gold); margin:8px 0;">%s</div>
                                    <span style="font-size:0.75rem; color:var(--green);">@ 12%% Compound Growth</span>
                                </div>
                                <div class="proj-card">
                                    <div style="font-size:0.85rem; color:var(--subtext);">📅 3-Year Projected Portfolio</div>
                                    <div style="font-size:1.7rem; font-weight:800; color:var(--purple); margin:8px 0;">%s</div>
                                    <span style="font-size:0.75rem; color:var(--green);">@ 12%% Compound Growth</span>
                                </div>
                                <div class="proj-card">
                                    <div style="font-size:0.85rem; color:var(--subtext);">📅 5-Year Projected Portfolio</div>
                                    <div style="font-size:1.7rem; font-weight:800; color:var(--primary); margin:8px 0;">%s</div>
                                    <span style="font-size:0.75rem; color:var(--green);">@ 12%% Compound Growth</span>
                                </div>
                            </div>
                        </div>

                        <div class="table-card">
                            <h3 style="margin-top:0; color:var(--primary);">💡 Non-Essential Money Leak Analyzer</h3>
                            <div class="leak-grid">%s</div>
                        </div>

                        <div class="table-card">
                            <h3 style="margin-top:0; color:var(--primary);">🚀 Recommended Investment Platforms</h3>
                            <div class="app-grid">%s</div>
                        </div>
                    </div>
                </div>

                <!-- MODAL 1: Add Expense / Income -->
                <div id="modal-txn" class="modal">
                    <div class="modal-content">
                        <h3 style="margin-top:0; color:var(--primary);">Log New Transaction</h3>
                        <form action="/api/add-transaction" method="POST">
                            <div class="form-group"><label>Description / Name</label><input type="text" name="desc" required placeholder="e.g. Course Textbooks"></div>
                            <div class="form-group"><label>Amount ($)</label><input type="number" step="0.01" name="amount" required placeholder="120.00"></div>
                            <div class="form-group"><label>Type</label>
                                <select name="isIncome"><option value="false">Expense</option><option value="true">Income</option></select>
                            </div>
                            <div class="form-group"><label>Category</label>
                                <select name="category">
                                    <option value="STUDY">🎓 Study & Education (Essential)</option>
                                    <option value="ESSENTIAL">⚡ Utilities & Health (Essential)</option>
                                    <option value="HOUSING">🏠 Housing & Rent (Essential)</option>
                                    <option value="FOOD_DINING">🍔 Dining Out (Non-essential)</option>
                                    <option value="ENTERTAINMENT">🎬 Entertainment (Non-essential)</option>
                                    <option value="TRAVEL">✈️ Leisure Travel (Non-essential)</option>
                                    <option value="SALARY">💼 Salary & Main Income</option>
                                </select>
                            </div>
                            <button type="submit" class="btn-action" style="width:100%%; margin-top:10px;">Submit Transaction</button>
                            <button type="button" onclick="closeModal('modal-txn')" style="width:100%%; background:transparent; border:none; color:var(--subtext); margin-top:10px; cursor:pointer;">Cancel</button>
                        </form>
                    </div>
                </div>

                <!-- MODAL 2: Edit Expense / Income -->
                <div id="modal-edit-txn" class="modal">
                    <div class="modal-content">
                        <h3 style="margin-top:0; color:var(--primary);">✏️ Edit Transaction</h3>
                        <form action="/api/edit-transaction" method="POST">
                            <input type="hidden" id="edit-id" name="id">
                            <div class="form-group"><label>Description / Name</label><input type="text" id="edit-desc" name="desc" required></div>
                            <div class="form-group"><label>Amount ($)</label><input type="number" step="0.01" id="edit-amount" name="amount" required></div>
                            <div class="form-group"><label>Type</label>
                                <select id="edit-isIncome" name="isIncome"><option value="false">Expense</option><option value="true">Income</option></select>
                            </div>
                            <div class="form-group"><label>Category</label>
                                <select id="edit-category" name="category">
                                    <option value="STUDY">🎓 Study & Education (Essential)</option>
                                    <option value="ESSENTIAL">⚡ Utilities & Health (Essential)</option>
                                    <option value="HOUSING">🏠 Housing & Rent (Essential)</option>
                                    <option value="FOOD_DINING">🍔 Dining Out (Non-essential)</option>
                                    <option value="ENTERTAINMENT">🎬 Entertainment (Non-essential)</option>
                                    <option value="TRAVEL">✈️ Leisure Travel (Non-essential)</option>
                                    <option value="SALARY">💼 Salary & Main Income</option>
                                </select>
                            </div>
                            <button type="submit" class="btn-action" style="width:100%%; margin-top:10px;">Save Edits</button>
                            <button type="button" onclick="closeModal('modal-edit-txn')" style="width:100%%; background:transparent; border:none; color:var(--subtext); margin-top:10px; cursor:pointer;">Cancel</button>
                        </form>
                    </div>
                </div>

                <!-- MODAL 3: Change Budget -->
                <div id="modal-budget" class="modal">
                    <div class="modal-content">
                        <h3 style="margin-top:0; color:var(--primary);">Change Monthly Budget Limit</h3>
                        <form action="/api/set-budget" method="POST">
                            <div class="form-group"><label>New Monthly Budget Amount ($)</label><input type="number" step="0.01" name="budget" value="%.2f" required></div>
                            <button type="submit" class="btn-action" style="width:100%%; margin-top:10px;">Save Budget Limit</button>
                            <button type="button" onclick="closeModal('modal-budget')" style="width:100%%; background:transparent; border:none; color:var(--subtext); margin-top:10px; cursor:pointer;">Cancel</button>
                        </form>
                    </div>
                </div>

                <!-- MODAL 4: Add Subscription -->
                <div id="modal-sub" class="modal">
                    <div class="modal-content">
                        <h3 style="margin-top:0; color:var(--primary);">Add New Subscription</h3>
                        <form action="/api/add-subscription" method="POST">
                            <div class="form-group"><label>Service Name</label><input type="text" name="name" required placeholder="e.g. Netflix"></div>
                            <div class="form-group"><label>Monthly Cost ($)</label><input type="number" step="0.01" name="cost" required placeholder="19.99"></div>
                            <div class="form-group"><label>Days Until Next Billing</label><input type="number" name="days" value="30" required></div>
                            <button type="submit" class="btn-action" style="width:100%%; margin-top:10px;">Add Subscription</button>
                            <button type="button" onclick="closeModal('modal-sub')" style="width:100%%; background:transparent; border:none; color:var(--subtext); margin-top:10px; cursor:pointer;">Cancel</button>
                        </form>
                    </div>
                </div>

                <!-- MODAL 5: Add Crypto Asset -->
                <div id="modal-asset" class="modal">
                    <div class="modal-content">
                        <h3 style="margin-top:0; color:var(--primary);">Add Asset to Portfolio</h3>
                        <form action="/api/add-asset" method="POST">
                            <div class="form-group"><label>Asset Symbol</label><input type="text" name="symbol" required placeholder="e.g. BTC"></div>
                            <div class="form-group"><label>Full Name</label><input type="text" name="name" required placeholder="e.g. Bitcoin"></div>
                            <div class="form-group"><label>Quantity Owned</label><input type="number" step="0.0001" name="qty" required placeholder="0.05"></div>
                            <div class="form-group"><label>Buy Price ($)</label><input type="number" step="0.01" name="buyPrice" required placeholder="60000"></div>
                            <div class="form-group"><label>Current Price ($)</label><input type="number" step="0.01" name="curPrice" required placeholder="65000"></div>
                            <div class="form-group"><label>Risk Level</label>
                                <select name="risk"><option value="LOW">LOW</option><option value="MEDIUM">MEDIUM</option><option value="HIGH">HIGH</option></select>
                            </div>
                            <button type="submit" class="btn-action" style="width:100%%; margin-top:10px;">Add Asset</button>
                            <button type="button" onclick="closeModal('modal-asset')" style="width:100%%; background:transparent; border:none; color:var(--subtext); margin-top:10px; cursor:pointer;">Cancel</button>
                        </form>
                    </div>
                </div>

                <!-- Floating AI Chatbot Bubble -->
                <button class="chat-bubble-btn" onclick="toggleChat()">💬</button>
                <div id="chat-window" class="chat-window">
                    <div class="chat-header">
                        <strong style="color:var(--primary);">🤖 AI Financial Advisor</strong>
                        <button onclick="toggleChat()" style="background:none; border:none; color:#fff; cursor:pointer;">✕</button>
                    </div>
                    <div class="chat-messages">%s</div>
                    <form action="/api/chat" method="POST" style="padding:10px; background:#0f172a; display:flex; gap:5px; margin:0;">
                        <input type="text" name="message" placeholder="Ask financial advisor..." required style="flex:1; background:#1e293b; border:1px solid var(--card-border); color:#fff; padding:8px; border-radius:8px; font-size:0.85rem;">
                        <button type="submit" class="btn-action">Send</button>
                    </form>
                </div>

                <script>
                    function openEditModal(id, desc, amount, category, isIncome) {
                        document.getElementById('edit-id').value = id;
                        document.getElementById('edit-desc').value = desc;
                        document.getElementById('edit-amount').value = amount;
                        document.getElementById('edit-category').value = category;
                        document.getElementById('edit-isIncome').value = isIncome;
                        openModal('modal-edit-txn');
                    }

                    function showPage(pageId) {
                        document.querySelectorAll('.page-section').forEach(el => el.classList.remove('active-page'));
                        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));

                        document.getElementById('page-' + pageId).classList.add('active-page');
                        document.getElementById('btn-' + pageId).classList.add('active');
                        localStorage.setItem('active_page_pref', pageId);
                    }

                    const savedPage = localStorage.getItem('active_page_pref') || 'finance';
                    showPage(savedPage);

                    function toggleTheme() {
                        document.body.classList.toggle('light-mode');
                        localStorage.setItem('theme_pref', document.body.classList.contains('light-mode') ? 'light' : 'dark');
                    }
                    if (localStorage.getItem('theme_pref') === 'light') {
                        document.body.classList.add('light-mode');
                    }
                    function toggleChat() {
                        const win = document.getElementById('chat-window');
                        win.style.display = (win.style.display === 'flex') ? 'none' : 'flex';
                    }
                    function openModal(id) { document.getElementById(id).style.display = 'flex'; }
                    function closeModal(id) { document.getElementById(id).style.display = 'none'; }
                </script>
            </body>
            </html>
            """.formatted(
                "USD".equals(currencyService.getSelectedCurrency()) ? "selected" : "",
                "EUR".equals(currencyService.getSelectedCurrency()) ? "selected" : "",
                "GBP".equals(currencyService.getSelectedCurrency()) ? "selected" : "",
                "INR".equals(currencyService.getSelectedCurrency()) ? "selected" : "",
                "JPY".equals(currencyService.getSelectedCurrency()) ? "selected" : "",
                currencyService.format(currentSavings), currencyService.format(totalInc),
                currencyService.format(essentialExp),
                currencyService.format(nonEssentialExp),
                currencyService.format(budget),
                // Bar Graph Heights
                140.0 - Math.min(120, (totalInc / 5000.0) * 120.0), Math.min(120, (totalInc / 5000.0) * 120.0),
                140.0 - Math.min(120, (expenseService.getTotalExpenses() / 5000.0) * 120.0), Math.min(120, (expenseService.getTotalExpenses() / 5000.0) * 120.0),
                140.0 - Math.min(120, (currentSavings / 5000.0) * 120.0), Math.min(120, (currentSavings / 5000.0) * 120.0),
                // Pie Chart Slices
                expenseService.getTotalExpenses() > 0 ? (essentialExp / expenseService.getTotalExpenses()) * 100.0 : 50.0,
                expenseService.getTotalExpenses() > 0 ? (nonEssentialExp / expenseService.getTotalExpenses()) * 100.0 : 50.0,
                expenseService.getTotalExpenses() > 0 ? (essentialExp / expenseService.getTotalExpenses()) * 100.0 : 50.0,
                currencyService.format(essentialExp), currencyService.format(nonEssentialExp),
                txnRows.toString(),
                currencyService.format(subscriptionService.calculateTotalMonthlyCost()), subscriptionService.getUrgentRenewals().size(),
                subRows.toString(),
                watchlistHtml.toString(),
                currencyService.format(cryptoService.calculateTotalPortfolioValue()), cryptoService.evaluateOverallPortfolioRisk().name(),
                cryptoRows.toString(),
                currencyService.format(potentialCut), currencyService.format(potentialSavings),
                currencyService.format(cryptoService.calculateProjectedGrowth(potentialSavings, 1, 12.0)),
                currencyService.format(cryptoService.calculateProjectedGrowth(potentialSavings, 3, 12.0)),
                currencyService.format(cryptoService.calculateProjectedGrowth(potentialSavings, 5, 12.0)),
                leakCards.toString(),
                appCards.toString(),
                budget,
                chatHtml.toString()
            );
    }
}
