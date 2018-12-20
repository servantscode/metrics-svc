package org.servantscode.metrics.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractBucket {
    private String name;
    private int value;

    public AbstractBucket(String name) {
        this.name = name;
        this.value = 0;
    }

    public abstract boolean itemFits(ResultSet rs) throws SQLException;

    public void increment() {
        value++;
    }

    // ----- Accessors -----
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}
