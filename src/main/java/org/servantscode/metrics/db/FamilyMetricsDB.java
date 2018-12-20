package org.servantscode.metrics.db;

import org.servantscode.metrics.MetricsResponse;
import org.servantscode.metrics.util.AbstractBucket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FamilyMetricsDB extends AbstractMetricsDB {

    public MetricsResponse getFamilySizes() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT count(1) FROM people GROUP BY family_id");
            List<AbstractBucket> buckets = generateFamilySizeBuckets();
            return generateResults(stmt, buckets, false);
        } catch (SQLException e) {
            throw new RuntimeException("Could generate age demographics", e);
        }
    }

    // ----- Private -----
    class NumberBucket extends AbstractBucket {
        int familySize;

        private NumberBucket(String description, int count) {
            super(description);
            this.familySize = count;
        }

        @Override
        public boolean itemFits(ResultSet rs) throws SQLException {
            return rs.getInt(1) == familySize;
        }
    }

    class MatchAllBucket extends AbstractBucket {

        private MatchAllBucket(String description) {
            super(description);
        }

        @Override
        public boolean itemFits(ResultSet rs) throws SQLException {
            return true;
        }
    }

    private List<AbstractBucket> generateFamilySizeBuckets() {
        LinkedList<AbstractBucket> buckets = new LinkedList<>();

        for(int i=1; i<10; i++) {
            buckets.add(new NumberBucket(Integer.toString(i), i));
        }
        buckets.add(new MatchAllBucket("10+"));

        return buckets;
    }

}
