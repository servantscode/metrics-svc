package org.servantscode.metrics;

import java.util.ArrayList;
import java.util.List;

public class MetricsResponse {
    private int totalRecords;
    private List<MetricEntry> data = new ArrayList<>();

    public class MetricEntry {
        String data;
        int count;

        public MetricEntry(String data, int count) {
            this.data = data;
            this.count = count;
        }

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public void addData(String data, int count) {
        this.data.add(new MetricEntry(data, count));
    }

    // ----- Accessors -----
    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public List<MetricEntry> getData() { return data; }
    public void setData(List<MetricEntry> data) { this.data = data; }
}
