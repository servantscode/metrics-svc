package org.servantscode.metrics;

public class PledgeMetricsResponse extends MetricsResponse {
    private float totalPledges;
    private float donationsToDate;

    // ----- Accessors -----
    public float getTotalPledges() { return totalPledges; }
    public void setTotalPledges(float totalPledges) { this.totalPledges = totalPledges; }

    public float getDonationsToDate() { return donationsToDate; }
    public void setDonationsToDate(float donationsToDate) { this.donationsToDate = donationsToDate; }
}
