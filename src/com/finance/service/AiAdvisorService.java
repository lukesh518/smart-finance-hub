package com.finance.service;

import com.finance.model.Transaction;
import com.finance.repository.FileRepository;
import java.io.IOException;
import java.util.*;

/**
 * Service Layer for AI Financial Guide Chatbot (Feature 2) & Non-Essential Expense Recommendation Engine (Feature 5).
 */
public class AiAdvisorService {
    private final List<String[]> chatHistory; // [Role, Message]

    public AiAdvisorService() {
        this.chatHistory = FileRepository.loadChatHistory();
        initWelcomeMessage();
    }

    public List<String[]> getChatHistory() {
        return Collections.unmodifiableList(chatHistory);
    }

    public String generateResponse(String userPrompt) throws IOException {
        chatHistory.add(new String[]{"user", userPrompt});

        String promptLower = userPrompt.toLowerCase();
        String advice;

        if (promptLower.contains("budget") || promptLower.contains("save")) {
            advice = "To optimize your budget, follow the 50/30/20 rule: 50% for Essentials (Rent, Tuition), 30% for Lifestyle, and 20% for Investments. Cut non-essential dining by 40% to boost your monthly savings rate.";
        } else if (promptLower.contains("invest") || promptLower.contains("crypto") || promptLower.contains("stock")) {
            advice = "Maintain a 60/30/10 portfolio strategy: 60% in Low-Risk core assets (BTC, ETH, S&P 500), 30% in Moderate-Risk growth assets (SOL, Tech Stocks), and 10% in Cash Reserve for market dips.";
        } else if (promptLower.contains("subscription") || promptLower.contains("bill")) {
            advice = "Audit active subscriptions monthly. Cancel unused OTT or gym trials at least 48 hours before renewal to avoid automatic credit card charges.";
        } else {
            advice = "I am your AI Personal Financial Advisor. I recommend keeping 3-6 months of emergency reserves, reducing non-essential food/travel spending by 35%, and investing consistent monthly savings into compound assets.";
        }

        chatHistory.add(new String[]{"bot", advice});
        FileRepository.saveChatHistory(chatHistory);
        return advice;
    }

    /**
     * Feature 5: Scans recent transactions and generates Top 3 Non-Essential Expense Reduction Tips.
     */
    public List<String[]> analyzeNonEssentialLeaks(List<Transaction> transactions) {
        Map<String, Double> nonEssentialTotals = new HashMap<>();

        for (Transaction t : transactions) {
            if (!t.isIncome() && !t.getCategory().isImportant()) {
                String catName = t.getCategory().getDisplayName();
                nonEssentialTotals.put(catName, nonEssentialTotals.getOrDefault(catName, 0.0) + t.getAmount());
            }
        }

        List<Map.Entry<String, Double>> sorted = new ArrayList<>(nonEssentialTotals.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<String[]> recommendations = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<String, Double> entry : sorted) {
            if (rank > 3) break;
            String category = entry.getKey();
            double totalSpent = entry.getValue();
            double potentialSaved = totalSpent * 0.40;

            String tip = switch (category) {
                case "Dining Out & Fast Food" -> String.format("Cooking meals at home 4 days/week cuts spending by $%.2f/month.", potentialSaved);
                case "Leisure Travel & Shopping" -> String.format("Using student discount passes & delaying impulse purchases saves $%.2f/month.", potentialSaved);
                case "Entertainment & Movies" -> String.format("Switching to shared family plans saves $%.2f/month.", potentialSaved);
                default -> String.format("Curbing 40%% discretionary spending in %s saves $%.2f/month.", category, potentialSaved);
            };

            recommendations.add(new String[]{
                String.valueOf(rank),
                category,
                String.format("$%.2f spent", totalSpent),
                tip,
                String.format("+$%.2f/mo", potentialSaved)
            });
            rank++;
        }

        return recommendations;
    }

    private void initWelcomeMessage() {
        if (chatHistory.isEmpty()) {
            chatHistory.add(new String[]{"bot", "Hello! I am your AI Financial Advisor. Ask me anything about budgeting, saving, or investing!"});
        }
    }
}
