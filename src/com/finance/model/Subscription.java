package com.finance.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Model representing active subscriptions, trial periods, and recurring bills.
 */
public class Subscription {
    private final String id;
    private String serviceName;
    private double monthlyCost;
    private LocalDate nextBillingDate;
    private String billingCycle; // "MONTHLY", "ANNUAL", "FREE_TRIAL"
    private boolean autoRenew;

    public Subscription(String serviceName, double monthlyCost, LocalDate nextBillingDate, String billingCycle, boolean autoRenew) {
        this.id = "SUB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.serviceName = serviceName;
        this.monthlyCost = Math.max(0, monthlyCost);
        this.nextBillingDate = nextBillingDate != null ? nextBillingDate : LocalDate.now().plusMonths(1);
        this.billingCycle = billingCycle != null ? billingCycle.toUpperCase() : "MONTHLY";
        this.autoRenew = autoRenew;
    }

    public Subscription(String id, String serviceName, double monthlyCost, LocalDate nextBillingDate, String billingCycle, boolean autoRenew) {
        this.id = id;
        this.serviceName = serviceName;
        this.monthlyCost = Math.max(0, monthlyCost);
        this.nextBillingDate = nextBillingDate != null ? nextBillingDate : LocalDate.now().plusMonths(1);
        this.billingCycle = billingCycle != null ? billingCycle.toUpperCase() : "MONTHLY";
        this.autoRenew = autoRenew;
    }

    public String getId() { return id; }
    public String getServiceName() { return serviceName; }
    public double getMonthlyCost() { return monthlyCost; }
    public LocalDate getNextBillingDate() { return nextBillingDate; }
    public String getBillingCycle() { return billingCycle; }
    public boolean isAutoRenew() { return autoRenew; }

    public long getDaysUntilRenewal() {
        return ChronoUnit.DAYS.between(LocalDate.now(), nextBillingDate);
    }

    public boolean isUrgentRenewal() {
        long days = getDaysUntilRenewal();
        return days >= 0 && days <= 5;
    }

    public String toCsvRow() {
        return String.join(",",
                id,
                "\"" + serviceName.replace("\"", "\"\"") + "\"",
                String.valueOf(monthlyCost),
                nextBillingDate.toString(),
                billingCycle,
                String.valueOf(autoRenew)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subscription)) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("[%s] ID: %s | %-20s | $%.2f/mo | Next Due: %s (%d days left)",
                billingCycle, id, serviceName, monthlyCost, nextBillingDate, getDaysUntilRenewal());
    }
}
