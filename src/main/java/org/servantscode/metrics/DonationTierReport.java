package org.servantscode.metrics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DonationTierReport {
    private int totalFamilies;
    private float totalDonations;
    private float averageFamilyDonation;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DonationTier> data = new LinkedList<>();

    public DonationTierReport(int totalFamilies, float totalDonations, LocalDate startDate, LocalDate endDate) {
        this.totalFamilies = totalFamilies;
        this.totalDonations = totalDonations;
        this.startDate = startDate;
        this.endDate = endDate;
        this.averageFamilyDonation = totalDonations/totalFamilies;
    }

    public class DonationTier {
        String name;
        int families;
        float percentFamilies;
        float total;
        float percentTotal;

        public DonationTier(String name, int families, float percentFamilies, float donations, float percentTotal) {
            this.name = name;
            this.families = families;
            this.percentFamilies = percentFamilies;
            this.total = donations;
            this.percentTotal = percentTotal;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getFamilies() { return families; }
        public void setFamilies(int families) { this.families = families; }

        public float getPercentFamilies() { return percentFamilies; }
        public void setPercentFamilies(float percentFamilies) { this.percentFamilies = percentFamilies; }

        public float getTotal() { return total; }
        public void setTotal(float total) { this.total = total; }

        public float getPercentTotal() { return percentTotal; }
        public void setPercentTotal(float percentTotal) { this.percentTotal = percentTotal; }
    }

    public void addData(String data, int families, float donations) {
        this.data.add(new DonationTier(data, families, (families * 100f)/totalFamilies, donations, (donations * 100f)/totalDonations));
    }

    // ----- Accessors -----
    public int getTotalFamilies() { return totalFamilies; }
    public void setTotalFamilies(int totalFamilies) { this.totalFamilies = totalFamilies; }

    public float getTotalDonations() { return totalDonations; }
    public void setTotalDonations(float totalDonations) { this.totalDonations = totalDonations; }

    public float getAverageFamilyDonation() { return averageFamilyDonation; }
    public void setAverageFamilyDonation(float averageFamilyDonation) { this.averageFamilyDonation = averageFamilyDonation; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<DonationTier> getData() { return data; }
    public void setData(List<DonationTier> data) { this.data = data; }
}
