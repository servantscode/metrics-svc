package org.servantscode.metrics.db;

import org.servantscode.commons.db.DBAccess;
import org.servantscode.metrics.MetricsResponse;
import org.servantscode.metrics.util.AbstractBucket;
import org.servantscode.metrics.util.Collector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class AbstractMetricsDB extends DBAccess {

    protected MetricsResponse generateResults(PreparedStatement stmt, List<AbstractBucket> buckets) throws SQLException {
        return generateResults(stmt, buckets, false, new MetricsResponse());
    }

    protected MetricsResponse generateResults(PreparedStatement stmt, List<AbstractBucket> buckets, boolean reverseOrder, Collector... collectors) throws SQLException {
        return generateResults(stmt, buckets, reverseOrder, new MetricsResponse(), collectors);
    }

    protected MetricsResponse generateResults(PreparedStatement stmt, List<AbstractBucket> buckets, boolean reverseOrder, MetricsResponse resp, Collector... collectors) throws SQLException {
        try(ResultSet rs = stmt.executeQuery()) {
            while(rs.next()) {
                for(AbstractBucket bucket: buckets) {
                    if(bucket.itemFits(rs)) {
                        bucket.increment();
                        break;
                    }
                }

                for(Collector collector: collectors)
                    collector.collect(rs);
            }

            if(reverseOrder)
                Collections.reverse(buckets);

            for(AbstractBucket bucket: buckets)
                resp.addData(bucket.getName(), bucket.getValue());

            return resp;
        }
    }
}
