package com.finance.service;

import com.finance.exceptions.InvalidAssetException;
import com.finance.model.Subscription;
import com.finance.repository.FileRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Layer for Subscription & Recurring Bill Management (Portal 2).
 */
public class SubscriptionService {
    private final List<Subscription> subscriptions;

    public SubscriptionService() {
        this.subscriptions = FileRepository.loadSubscriptions();
    }

    public void addSubscription(Subscription sub) throws InvalidAssetException, IOException {
        if (sub.getServiceName() == null || sub.getServiceName().trim().isEmpty()) {
            throw new InvalidAssetException("Service name cannot be empty.");
        }
        subscriptions.add(sub);
        FileRepository.saveSubscriptions(subscriptions);
    }

    public boolean removeSubscription(String id) throws IOException {
        boolean removed = subscriptions.removeIf(s -> s.getId().equalsIgnoreCase(id));
        if (removed) FileRepository.saveSubscriptions(subscriptions);
        return removed;
    }

    public List<Subscription> getAllSubscriptions() {
        return Collections.unmodifiableList(subscriptions);
    }

    public List<Subscription> getUrgentRenewals() {
        return subscriptions.stream().filter(Subscription::isUrgentRenewal).collect(Collectors.toList());
    }

    public double calculateTotalMonthlyCost() {
        return subscriptions.stream().mapToDouble(Subscription::getMonthlyCost).sum();
    }

    public double calculateTotalAnnualCost() {
        return calculateTotalMonthlyCost() * 12.0;
    }
}
