package org.servantscode.metrics.db;

import org.joda.time.LocalDate;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.metrics.MetricsResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PeopleMetricsDB extends DBAccess {

    public MetricsResponse getAges() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT birthdate FROM people");

            List<AgeDemographicBucket> buckets = generateDivisions();
            int totalCount = 0;

            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    Date d = rs.getDate(1);
                    totalCount++;

                    if(d == null) break;

                    for(AgeDemographicBucket bucket: buckets) {
                        if(bucket.startDate.before(d)) {
                            bucket.count++;
                            break;
                        }
                    }
                }

                MetricsResponse resp = new MetricsResponse();
                resp.setTotalRecords(totalCount);
                for(AgeDemographicBucket bucket: buckets)
                    resp.addData(bucket.ageRange, bucket.count);

                return resp;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could generate age demographics", e);
        }
    }

    private ArrayList<AgeDemographicBucket> generateDivisions() {
        ArrayList<AgeDemographicBucket> buckets = new ArrayList<>(9);

        LocalDate now = new LocalDate();
        buckets.add(new AgeDemographicBucket("0-6", now.plusYears(-7).toDate()));
        buckets.add(new AgeDemographicBucket("7-11", now.plusYears(-12).toDate()));
        buckets.add(new AgeDemographicBucket("12-17", now.plusYears(-18).toDate()));
        buckets.add(new AgeDemographicBucket("18-24", now.plusYears(-25).toDate()));
        buckets.add(new AgeDemographicBucket("25-34", now.plusYears(-35).toDate()));
        buckets.add(new AgeDemographicBucket("35-44", now.plusYears(-45).toDate()));
        buckets.add(new AgeDemographicBucket("45-54", now.plusYears(-55).toDate()));
        buckets.add(new AgeDemographicBucket("55-64", now.plusYears(-65).toDate()));
        buckets.add(new AgeDemographicBucket("65+", now.plusYears(-200).toDate()));

        return buckets;
    }

    class AgeDemographicBucket {
        String ageRange;
        Date startDate;
        int count;

        private AgeDemographicBucket(String ageRange, Date startDate) {
            this.ageRange = ageRange;
            this.startDate = startDate;
            this.count = 0;
        }
    }
}
