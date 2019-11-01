package org.servantscode.metrics;

public class PledgeMetricsResponse extends MetricsResponse {
    private float totalPledges;
    private float donationsToDate;
    private float pledgedDonations;
    private float unpledgedDonations;
    private float pledgedTarget;

    // ----- Accessors -----
    public float getTotalPledges() { return totalPledges; }
    public void setTotalPledges(float totalPledges) { this.totalPledges = totalPledges; }

    public float getDonationsToDate() { return donationsToDate; }
    public void setDonationsToDate(float donationsToDate) { this.donationsToDate = donationsToDate; }

    public float getPledgedDonations() { return pledgedDonations; }
    public void setPledgedDonations(float pledgedDonations) { this.pledgedDonations = pledgedDonations; }

    public float getUnpledgedDonations() { return unpledgedDonations; }
    public void setUnpledgedDonations(float unpledgedDonations) { this.unpledgedDonations = unpledgedDonations; }

    public float getPledgedTarget() { return pledgedTarget; }
    public void setPledgedTarget(float pledgedTarget) { this.pledgedTarget = pledgedTarget; }
}
