package org.servantscode.metrics;

public class PledgeMetricsResponse extends MetricsResponse {
    private float pledgedDonations;
    private float pledgedTarget;

    // ----- Accessors -----
    public float getPledgedDonations() { return pledgedDonations; }
    public void setPledgedDonations(float pledgedDonations) { this.pledgedDonations = pledgedDonations; }

    public float getPledgedTarget() { return pledgedTarget; }
    public void setPledgedTarget(float pledgedTarget) { this.pledgedTarget = pledgedTarget; }
}
