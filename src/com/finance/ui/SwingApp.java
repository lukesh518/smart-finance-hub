package com.finance.ui;

import com.finance.exceptions.BudgetBreachException;
import com.finance.model.*;
import com.finance.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Unified AI-Enhanced 4-Tab Desktop Application GUI for Wealth & Asset Management.
 */
public class SwingApp extends JFrame {
    private final ExpenseService expenseService;
    private final SubscriptionService subscriptionService;
    private final CryptoService cryptoService;

    // Portal 1: Expense Tracker UI
    private JTable txnTable;
    private DefaultTableModel txnTableModel;
    private JLabel totalIncomeLabel, essentialExpLabel, nonEssentialExpLabel, netSavingsLabel, budgetStatusLabel;

    // Portal 2: AI Advisor & Compound Growth Simulator UI
    private JLabel recSavingsLabel, growth1YrLabel, growth3YrLabel, growth5YrLabel;

    // Portal 3: Subscription UI
    private JTable subTable;
    private DefaultTableModel subTableModel;
    private JLabel totalSubCostLabel, urgentRenewalsLabel;

    // Portal 4: Crypto Portfolio UI
    private JTable cryptoTable;
    private DefaultTableModel cryptoTableModel;
    private JLabel portfolioValueLabel, portfolioProfitLabel, riskStatusLabel;

    public SwingApp(ExpenseService expenseService, SubscriptionService subscriptionService, CryptoService cryptoService) {
        this.expenseService = expenseService;
        this.subscriptionService = subscriptionService;
        this.cryptoService = cryptoService;

        setTitle("AI Smart Wealth & Investment Advisory Desktop App");
        setSize(1050, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        refreshAllPortals();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("📊 Expense Ledger (Dual-Priority)", createExpensePortal());
        tabbedPane.addTab("🤖 AI Wealth & Compound Growth Simulator", createAiSimulatorPortal());
        tabbedPane.addTab("🔄 Subscription Sentinel", createSubscriptionPortal());
        tabbedPane.addTab("⚡ Crypto & Stock Risk Analyzer", createCryptoPortal());

        add(tabbedPane);
    }

    // ==========================================
    // PORTAL 1: EXPENSE LEDGER (DUAL PRIORITY)
    // ==========================================
    private JPanel createExpensePortal() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel metricsPanel = new JPanel(new GridLayout(2, 3, 10, 8));
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Financial Summary & Dual-Priority Breakdown"));

        totalIncomeLabel = createBoldLabel("Total Income: $0.00", new Color(34, 139, 34));
        essentialExpLabel = createBoldLabel("🎓 Important (Study/Rent): $0.00", new Color(0, 102, 204));
        nonEssentialExpLabel = createBoldLabel("✈️ Non-Essential (Dining/Travel): $0.00", new Color(178, 34, 34));
        netSavingsLabel = createBoldLabel("Net Monthly Savings: $0.00", new Color(34, 139, 34));
        budgetStatusLabel = createBoldLabel("Budget Status: OK", Color.DARK_GRAY);

        metricsPanel.add(totalIncomeLabel);
        metricsPanel.add(essentialExpLabel);
        metricsPanel.add(nonEssentialExpLabel);
        metricsPanel.add(netSavingsLabel);
        metricsPanel.add(budgetStatusLabel);

        panel.add(metricsPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Priority Class", "Description", "Amount", "Category", "Date"};
        txnTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        txnTable = new JTable(txnTableModel);
        txnTable.setRowHeight(24);
        panel.add(new JScrollPane(txnTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton addIncomeBtn = new JButton("➕ Log Income");
        addIncomeBtn.addActionListener(e -> showAddTxnDialog(true));

        JButton addExpenseBtn = new JButton("➖ Log Expense");
        addExpenseBtn.addActionListener(e -> showAddTxnDialog(false));

        JButton setBudgetBtn = new JButton("⚙️ Set Monthly Budget");
        setBudgetBtn.addActionListener(e -> {
            String str = JOptionPane.showInputDialog(this, "Enter Monthly Budget Limit ($):", expenseService.getMonthlyBudget());
            if (str != null) {
                try {
                    expenseService.setMonthlyBudget(Double.parseDouble(str.trim()));
                    refreshAllPortals();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid budget amount.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnPanel.add(addIncomeBtn);
        btnPanel.add(addExpenseBtn);
        btnPanel.add(setBudgetBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // PORTAL 2: AI WEALTH & COMPOUND GROWTH SIMULATOR
    // ==========================================
    private JPanel createAiSimulatorPortal() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel recBox = new JPanel(new GridLayout(2, 1, 5, 5));
        recBox.setBorder(BorderFactory.createTitledBorder("💡 AI Savings Optimization Recommendation"));
        recSavingsLabel = createBoldLabel("Potential Savings Capacity: $0.00/mo", new Color(128, 0, 128));
        recSavingsLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        recBox.add(recSavingsLabel);
        recBox.add(new JLabel("Strategy: Curb 40% of non-essential discretionary expenses (dining out, travel, shopping)."));
        panel.add(recBox, BorderLayout.NORTH);

        JPanel projGrid = new JPanel(new GridLayout(1, 3, 15, 15));
        projGrid.setBorder(BorderFactory.createTitledBorder("📈 Compound Wealth Growth Simulation (Estimated @ 12% Annual Return)"));

        JPanel p1 = createProjectionBox("📅 1-Year Wealth Projection", growth1YrLabel = new JLabel("$0.00"));
        JPanel p2 = createProjectionBox("📅 3-Year Wealth Projection", growth3YrLabel = new JLabel("$0.00"));
        JPanel p3 = createProjectionBox("📅 5-Year Wealth Projection", growth5YrLabel = new JLabel("$0.00"));

        projGrid.add(p1); projGrid.add(p2); projGrid.add(p3);
        panel.add(projGrid, BorderLayout.CENTER);

        JTextArea allocText = new JTextArea("""
            🎯 Recommended Target Asset Allocation Strategy:
            -----------------------------------------------------------------------------
            • 60% Low Risk Assets (Bitcoin BTC / Ethereum ETH / Index Funds): Stable compounding core
            • 30% Moderate Risk Assets (Solana SOL / Tech Growth Stocks): High growth potential
            • 10% Cash Reserve / Emergency Safety Net: Immediate liquidity
            """);
        allocText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        allocText.setEditable(false);
        allocText.setBackground(new Color(245, 245, 250));
        allocText.setBorder(BorderFactory.createTitledBorder("💡 Asset Allocation Advice"));
        panel.add(allocText, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createProjectionBox(String title, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 2));
        p.setBackground(Color.WHITE);

        JLabel tLabel = new JLabel(title, SwingConstants.CENTER);
        tLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tLabel.setForeground(Color.GRAY);

        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(new Color(0, 102, 204));

        p.add(tLabel, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    // ==========================================
    // PORTAL 3: SUBSCRIPTION SENTINEL
    // ==========================================
    private JPanel createSubscriptionPortal() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        totalSubCostLabel = createBoldLabel("Total Subscriptions: $0.00/mo", new Color(128, 0, 128));
        urgentRenewalsLabel = createBoldLabel("Urgent Renewals (<= 5 Days): 0", new Color(220, 20, 60));
        headerPanel.add(totalSubCostLabel);
        headerPanel.add(urgentRenewalsLabel);
        panel.add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Service Name", "Monthly Cost", "Next Billing Date", "Days Left", "Alert Status"};
        subTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        subTable = new JTable(subTableModel);
        subTable.setRowHeight(24);
        panel.add(new JScrollPane(subTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton addSubBtn = new JButton("➕ Add Subscription");
        addSubBtn.addActionListener(e -> showAddSubDialog());
        btnPanel.add(addSubBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // PORTAL 4: CRYPTO & STOCK RISK ANALYZER
    // ==========================================
    private JPanel createCryptoPortal() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        headerPanel.setBorder(BorderFactory.createTitledBorder("Portfolio Valuation & Risk Dashboard"));

        portfolioValueLabel = createBoldLabel("Portfolio Value: $0.00", new Color(0, 102, 204));
        portfolioProfitLabel = createBoldLabel("Total Profit/Loss: $0.00", new Color(34, 139, 34));
        riskStatusLabel = createBoldLabel("Risk Assessment: LOW", new Color(255, 140, 0));

        headerPanel.add(portfolioValueLabel);
        headerPanel.add(portfolioProfitLabel);
        headerPanel.add(riskStatusLabel);
        panel.add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Symbol", "Asset Name", "Quantity", "Buy Price", "Current Price", "Current Value", "Profit/Loss", "Risk Level"};
        cryptoTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        cryptoTable = new JTable(cryptoTableModel);
        cryptoTable.setRowHeight(24);
        panel.add(new JScrollPane(cryptoTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton addAssetBtn = new JButton("⚡ Add Crypto / Stock Asset");
        addAssetBtn.addActionListener(e -> showAddCryptoDialog());
        btnPanel.add(addAssetBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // REFRESH DATA & DIALOGS
    // ==========================================
    private void refreshAllPortals() {
        // Portal 1 Refresh
        txnTableModel.setRowCount(0);
        for (Transaction t : expenseService.getAllTransactions()) {
            String pClass = t.isIncome() ? "INCOME" : (t.getCategory().isImportant() ? "🎓 IMPORTANT/STUDY" : "✈️ NON-ESSENTIAL");
            txnTableModel.addRow(new Object[]{
                    t.getId(), pClass, t.getDescription(),
                    String.format("$%.2f", t.getAmount()), t.getCategory().getDisplayName(), t.getDate()
            });
        }
        double inc = expenseService.getTotalIncome();
        double ess = expenseService.getImportantExpensesTotal();
        double nonEss = expenseService.getNonEssentialExpensesTotal();
        double net = expenseService.getNetSavings();
        double bud = expenseService.getMonthlyBudget();

        totalIncomeLabel.setText(String.format("Total Income: $%.2f", inc));
        essentialExpLabel.setText(String.format("🎓 Important (Study/Rent): $%.2f", ess));
        nonEssentialExpLabel.setText(String.format("✈️ Non-Essential (Dining/Travel): $%.2f", nonEss));
        netSavingsLabel.setText(String.format("Net Savings: $%.2f", net));
        if (bud > 0 && (ess + nonEss) > bud) {
            budgetStatusLabel.setText(String.format("⚠️ OVER BUDGET! ($%.2f)", (ess + nonEss) - bud));
            budgetStatusLabel.setForeground(Color.RED);
        } else {
            budgetStatusLabel.setText(String.format("Budget Status: OK ($%.2f Limit)", bud));
            budgetStatusLabel.setForeground(new Color(34, 139, 34));
        }

        // Portal 2 (AI Simulator Refresh)
        double potSavings = expenseService.getRecommendedMonthlySavings();
        recSavingsLabel.setText(String.format("Potential Monthly Savings Capacity: $%.2f/mo (+$%.2f/mo non-essential cut)", potSavings, nonEss * 0.40));
        growth1YrLabel.setText(String.format("$%.2f", cryptoService.calculateProjectedGrowth(potSavings, 1, 12.0)));
        growth3YrLabel.setText(String.format("$%.2f", cryptoService.calculateProjectedGrowth(potSavings, 3, 12.0)));
        growth5YrLabel.setText(String.format("$%.2f", cryptoService.calculateProjectedGrowth(potSavings, 5, 12.0)));

        // Portal 3 Refresh
        subTableModel.setRowCount(0);
        List<Subscription> subs = subscriptionService.getAllSubscriptions();
        for (Subscription s : subs) {
            subTableModel.addRow(new Object[]{
                    s.getId(), s.getServiceName(), String.format("$%.2f", s.getMonthlyCost()),
                    s.getNextBillingDate(), s.getDaysUntilRenewal() + " Days", s.isUrgentRenewal() ? "⚠️ DUE SOON" : "OK"
            });
        }
        totalSubCostLabel.setText(String.format("Total Subscriptions: $%.2f/mo ($%.2f/yr)",
                subscriptionService.calculateTotalMonthlyCost(), subscriptionService.calculateTotalAnnualCost()));
        urgentRenewalsLabel.setText("Urgent Renewals (<= 5 Days): " + subscriptionService.getUrgentRenewals().size());

        // Portal 4 Refresh
        cryptoTableModel.setRowCount(0);
        for (CryptoAsset a : cryptoService.getAllAssets()) {
            cryptoTableModel.addRow(new Object[]{
                    a.getId(), a.getSymbol(), a.getAssetName(), String.format("%.4f", a.getQuantity()),
                    String.format("$%.2f", a.getBuyPrice()), String.format("$%.2f", a.getCurrentPrice()),
                    String.format("$%.2f", a.getCurrentMarketValue()),
                    String.format("%+.2f (%.1f%%)", a.getProfitLoss(), a.getProfitLossPercentage()),
                    a.getRiskLevel().name()
            });
        }
        double portValue = cryptoService.calculateTotalPortfolioValue();
        double portPL = cryptoService.calculateTotalProfitLoss();
        portfolioValueLabel.setText(String.format("Portfolio Value: $%.2f", portValue));
        portfolioProfitLabel.setText(String.format("Total Profit/Loss: %+.2f", portPL));
        portfolioProfitLabel.setForeground(portPL >= 0 ? new Color(34, 139, 34) : Color.RED);
        riskStatusLabel.setText("Risk Assessment: " + cryptoService.evaluateOverallPortfolioRisk().name());
    }

    private void showAddTxnDialog(boolean isIncome) {
        JTextField descField = new JTextField();
        JTextField amountField = new JTextField();
        JComboBox<Category> catCombo = new JComboBox<>(Category.values());

        Object[] message = {
                "Description:", descField,
                "Amount ($):", amountField,
                "Category (Important vs Non-Essential):", catCombo
        };

        int option = JOptionPane.showConfirmDialog(this, message, isIncome ? "Log Income" : "Log Expense", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String desc = descField.getText().trim();
                double amount = Double.parseDouble(amountField.getText().trim());
                Category cat = (Category) catCombo.getSelectedItem();
                Transaction txn = new Transaction(desc, amount, LocalDate.now(), cat, isIncome);

                try {
                    expenseService.addTransaction(txn, false);
                    refreshAllPortals();
                } catch (BudgetBreachException ex) {
                    int confirm = JOptionPane.showConfirmDialog(this, ex.getMessage() + "\nLog transaction anyway?", "Budget Warning", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        expenseService.addTransaction(txn, true);
                        refreshAllPortals();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddSubDialog() {
        JTextField nameField = new JTextField();
        JTextField costField = new JTextField();
        JTextField daysField = new JTextField("30");
        JCheckBox autoRenewCheck = new JCheckBox("Auto Renew Enabled", true);

        Object[] message = {
                "Service Name:", nameField,
                "Monthly Cost ($):", costField,
                "Days Until Next Renewal:", daysField,
                autoRenewCheck
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Subscription", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                double cost = Double.parseDouble(costField.getText().trim());
                int days = Integer.parseInt(daysField.getText().trim());
                LocalDate nextDate = LocalDate.now().plusDays(days);

                Subscription sub = new Subscription(name, cost, nextDate, "MONTHLY", autoRenewCheck.isSelected());
                subscriptionService.addSubscription(sub);
                refreshAllPortals();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddCryptoDialog() {
        JTextField symbolField = new JTextField("BTC");
        JTextField nameField = new JTextField("Bitcoin");
        JTextField qtyField = new JTextField("0.05");
        JTextField buyPriceField = new JTextField("60000");
        JTextField curPriceField = new JTextField("65000");
        JComboBox<PortfolioRisk> riskCombo = new JComboBox<>(PortfolioRisk.values());

        Object[] message = {
                "Asset Symbol:", symbolField,
                "Asset Full Name:", nameField,
                "Quantity Owned:", qtyField,
                "Buy Price ($):", buyPriceField,
                "Current Price ($):", curPriceField,
                "Risk Classification:", riskCombo
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Asset", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String symbol = symbolField.getText().trim();
                String name = nameField.getText().trim();
                double qty = Double.parseDouble(qtyField.getText().trim());
                double buyPrice = Double.parseDouble(buyPriceField.getText().trim());
                double curPrice = Double.parseDouble(curPriceField.getText().trim());
                PortfolioRisk risk = (PortfolioRisk) riskCombo.getSelectedItem();

                CryptoAsset asset = new CryptoAsset(symbol, name, qty, buyPrice, curPrice, risk);
                cryptoService.addAsset(asset);
                refreshAllPortals();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JLabel createBoldLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(color);
        return label;
    }
}
