package org.servantscode.metrics;

import java.util.ArrayList;
import java.util.List;

public class MetricsResponse {
    private int totalRecords;
    private List<MetricEntry> data = new ArrayList<>();

    public class MetricEntry {
        String name;
        int value;

        public MetricEntry(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
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
