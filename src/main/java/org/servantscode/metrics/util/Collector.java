package org.servantscode.metrics.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Collector {
    void collect(ResultSet rs) throws SQLException;
}
