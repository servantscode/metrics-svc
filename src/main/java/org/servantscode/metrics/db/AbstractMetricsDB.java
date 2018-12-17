package org.servantscode.metrics.db;

import org.servantscode.commons.db.DBAccess;
import org.servantscode.metrics.MetricsResponse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AbstractMetricsDB extends DBAccess {

    protected MetricsResponse generateResults(PreparedStatement stmt, List<AbstractBucket> buckets) throws SQLException {
        return generateResults(stmt, buckets, false);
    }

    protected MetricsResponse generateResults(PreparedStatement stmt, List<AbstractBucket> buckets, boolean reverseOrder) throws SQLException {
        int totalCount = 0;
        try(ResultSet rs = stmt.executeQuery()) {
            while(rs.next()) {
                totalCount++;
                for(AbstractBucket bucket: buckets) {
                    if(bucket.itemFits(rs)) {
                        bucket.increment();
                        break;
                    }
                }
            }

            if(reverseOrder)
                Collections.reverse(buckets);

            MetricsResponse resp = new MetricsResponse();
            resp.setTotalRecords(totalCount);
            for(AbstractBucket bucket: buckets)
                resp.addData(bucket.getName(), bucket.getValue());

            return resp;
        }
    }
}
