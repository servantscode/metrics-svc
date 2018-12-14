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
import java.util.LinkedList;
import java.util.List;

public class PeopleMetricsDB extends DBAccess {
    private static final int MAX_AGE=200;

    public MetricsResponse getAges() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT birthdate FROM people");
            List<DateRangeBucket> buckets = generateAgeDivisions();
            return generateResults(stmt, buckets);
        } catch (SQLException e) {
            throw new RuntimeException("Could generate age demographics", e);
        }
    }

    public MetricsResponse getMembershipLength() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT member_since FROM people");
            List<DateRangeBucket> buckets = generateLongevityDivisions();
            return generateResults(stmt, buckets);
        } catch (SQLException e) {
            throw new RuntimeException("Could generate age demographics", e);
        }
    }

    public MetricsResponse getNewYearlyMembership() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT member_since FROM people");
            List<DateRangeBucket> buckets = generateYearlyDivisions(20);
            return generateResults(stmt, buckets);
        } catch (SQLException e) {
            throw new RuntimeException("Could generate age demographics", e);
        }
    }

    public MetricsResponse getNewMonthlyMembership() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT member_since FROM people");
            List<DateRangeBucket> buckets = generateMonthlyDivisions(24);
            return generateResults(stmt, buckets);
        } catch (SQLException e) {
            throw new RuntimeException("Could generate age demographics", e);
        }
    }


    // ----- Private -----
    class DateRangeBucket {
        String description;
        Date startDate;
        int count;

        private DateRangeBucket(String description, Date startDate) {
            this.description = description;
            this.startDate = startDate;
            this.count = 0;
        }
    }

    private MetricsResponse generateResults(PreparedStatement stmt, List<DateRangeBucket> buckets) throws SQLException {
        int totalCount = 0;
        try(ResultSet rs = stmt.executeQuery()) {
            while(rs.next()) {
                Date d = rs.getDate(1);
                totalCount++;

                if(d == null) break;

                for(DateRangeBucket bucket: buckets) {
                    if(bucket.startDate.before(d)) {
                        bucket.count++;
                        break;
                    }
                }
            }

            MetricsResponse resp = new MetricsResponse();
            resp.setTotalRecords(totalCount);
            for(DateRangeBucket bucket: buckets)
                resp.addData(bucket.description, bucket.count);

            return resp;
        }
    }

    private List<DateRangeBucket> generateYearlyDivisions(int numberOfYears) {
        LinkedList<DateRangeBucket> buckets = new LinkedList<>();

        LocalDate startOfYear = new LocalDate().withDayOfYear(1);
        buckets.add(new DateRangeBucket(Integer.toString(startOfYear.getYear()), startOfYear.toDate()));
        for(int i=1; i<numberOfYears; i++) {
            LocalDate date = startOfYear.plusYears(-i);
            buckets.add(new DateRangeBucket(date.toString("yyyy"), date.toDate()));
        }

        return buckets;
    }

    private List<DateRangeBucket> generateMonthlyDivisions(int numberOfMonths) {
        LinkedList<DateRangeBucket> buckets = new LinkedList<>();

        LocalDate startOfMonth = new LocalDate().withDayOfMonth(1);
        for(int i=0; i<numberOfMonths; i++) {
            LocalDate date = startOfMonth.plusMonths(-i);
            buckets.add(new DateRangeBucket(date.toString("MMMM yyyy"), date.toDate()));
        }

        return buckets;
    }

    private ArrayList<DateRangeBucket> generateLongevityDivisions() {
        ArrayList<DateRangeBucket> buckets = new ArrayList<>(9);

        LocalDate now = new LocalDate();
        buckets.add(new DateRangeBucket("<1", now.plusYears(-1).toDate()));
        buckets.add(new DateRangeBucket("1-4", now.plusYears(-5).toDate()));
        buckets.add(new DateRangeBucket("5-9", now.plusYears(-10).toDate()));
        buckets.add(new DateRangeBucket("10-19", now.plusYears(-20).toDate()));
        buckets.add(new DateRangeBucket("20-29", now.plusYears(-30).toDate()));
        buckets.add(new DateRangeBucket("30-39", now.plusYears(-40).toDate()));
        buckets.add(new DateRangeBucket("40-49", now.plusYears(-50).toDate()));
        buckets.add(new DateRangeBucket("50+", now.plusYears(-MAX_AGE).toDate()));

        return buckets;
    }

    private ArrayList<DateRangeBucket> generateAgeDivisions() {
        ArrayList<DateRangeBucket> buckets = new ArrayList<>(9);

        LocalDate now = new LocalDate();
        buckets.add(new DateRangeBucket("0-6", now.plusYears(-7).toDate()));
        buckets.add(new DateRangeBucket("7-11", now.plusYears(-12).toDate()));
        buckets.add(new DateRangeBucket("12-17", now.plusYears(-18).toDate()));
        buckets.add(new DateRangeBucket("18-24", now.plusYears(-25).toDate()));
        buckets.add(new DateRangeBucket("25-34", now.plusYears(-35).toDate()));
        buckets.add(new DateRangeBucket("35-44", now.plusYears(-45).toDate()));
        buckets.add(new DateRangeBucket("45-54", now.plusYears(-55).toDate()));
        buckets.add(new DateRangeBucket("55-64", now.plusYears(-65).toDate()));
        buckets.add(new DateRangeBucket("65+", now.plusYears(-MAX_AGE).toDate()));

        return buckets;
    }
}
