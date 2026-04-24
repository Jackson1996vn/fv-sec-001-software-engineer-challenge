package com.campaign;

public class CampaignMetrics {
    private long totalImpressions;
    private long totalClicks;
    private double totalSpend;
    private long totalConversions;

    public void addRow(long impressions, long clicks, double spend, long conversions) {
        this.totalImpressions += impressions;
        this.totalClicks += clicks;
        this.totalSpend += spend;
        this.totalConversions += conversions;
    }

    public double getCtr() {
        if (totalImpressions == 0) return 0.0;
        return (double) totalClicks / totalImpressions;
    }

    public Double getCpa() {
        if (totalConversions == 0) return null;
        return totalSpend / totalConversions;
    }

    public long getTotalImpressions() { return totalImpressions; }
    public long getTotalClicks() { return totalClicks; }
    public double getTotalSpend() { return totalSpend; }
    public long getTotalConversions() { return totalConversions; }
}
