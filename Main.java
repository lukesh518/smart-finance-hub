package com.finance;

import com.finance.service.*;
import com.finance.ui.SwingApp;
import com.finance.web.WebServer;

import javax.swing.SwingUtilities;

/**
 * Main Application Launcher for Smart Finance Hub.
 * Starts BOTH the 7-Feature Web Application (http://localhost:8080) and Desktop GUI App.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("  STARTING SMART PERSONAL FINANCE WEALTH HUB...     ");
        System.out.println("====================================================");

        // Initialize Services across all 7 Features
        ExpenseService expenseService = new ExpenseService();
        SubscriptionService subscriptionService = new SubscriptionService();
        CryptoService cryptoService = new CryptoService();
        CurrencyService currencyService = new CurrencyService();
        CryptoWatchlistService watchlistService = new CryptoWatchlistService();
        AiAdvisorService aiAdvisorService = new AiAdvisorService();

        // 1. Start 7-Feature Web Server Application at http://localhost:8080
        try {
            WebServer webServer = new WebServer(
                    expenseService, subscriptionService, cryptoService,
                    currencyService, watchlistService, aiAdvisorService, 8080
            );
            webServer.start();
        } catch (Exception e) {
            System.err.println("Notice: Web Server setup error: " + e.getMessage());
        }

        // 2. Launch Desktop Application Window
        SwingUtilities.invokeLater(() -> {
            SwingApp app = new SwingApp(expenseService, subscriptionService, cryptoService);
            app.setVisible(true);
            app.toFront();
            app.requestFocus();
            System.out.println("-> Desktop App Window successfully opened on screen!");
        });
    }
}
