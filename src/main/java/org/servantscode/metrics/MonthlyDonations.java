package org.servantscode.metrics;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MonthlyDonations {

    private ZonedDateTime month;
    private String label;
    private float totalDonations;
    private float pledged;
    private float unpledged;

    public MonthlyDonations(ZonedDateTime month) {
        if(month == null)
            throw new IllegalArgumentException();

        this.month = month;
        this.label = month.format(DateTimeFormatter.ofPattern("MMM yyyy"));
    }

    public void addPledged(float amount) {
        pledged = amount;
        totalDonations += amount;
        totalDonations = Math.round(totalDonations*100)/100f;
    }

    public void addUnpledged(float amount) {
        unpledged = amount;
        totalDonations += amount;
        totalDonations = Math.round(totalDonations*100)/100f;
    }

    // ----- Accessors -----
    public ZonedDateTime getMonth() { return month; }
    public void setMonth(ZonedDateTime month) { this.month = month; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public float getTotalDonations() { return totalDonations; }
    public void setTotalDonations(float totalDonations) { this.totalDonations = totalDonations; }

    public float getPledged() { return pledged; }
    public void setPledged(float pledged) { this.pledged = pledged; }

    public float getUnpledged() { return unpledged; }
    public void setUnpledged(float unpledged) { this.unpledged = unpledged; }
}
