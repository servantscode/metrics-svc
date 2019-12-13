package org.servantscode.metrics;

import java.time.LocalDate;

public class DonationReport {

    private LocalDate startDate;
    private LocalDate endDate;
    private int fundId;

    private float totalDonations;
    private float pledged;
    private float unpledged;

    public DonationReport() { }

    public void addPledged(float amount) {
        pledged += amount;
        totalDonations += amount;
        totalDonations = Math.round(totalDonations*100)/100f;
    }

    public void addUnpledged(float amount) {
        unpledged += amount;
        totalDonations += amount;
        totalDonations = Math.round(totalDonations*100)/100f;
    }

    // ----- Accessors -----
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public float getTotalDonations() { return totalDonations; }
    public void setTotalDonations(float totalDonations) { this.totalDonations = totalDonations; }

    public float getPledged() { return pledged; }
    public void setPledged(float pledged) { this.pledged = pledged; }

    public float getUnpledged() { return unpledged; }
    public void setUnpledged(float unpledged) { this.unpledged = unpledged; }

    public void setFundId(int fundId) { this.fundId = fundId; }
    public int getFundId() { return fundId; }
}
