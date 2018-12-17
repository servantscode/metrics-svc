package org.servantscode.metrics.db;

import org.joda.time.LocalDate;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.metrics.MetricsResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PeopleMetricsDB extends AbstractMetricsDB {
    private static final int MAX_AGE = 200;

    public MetricsResponse getAges() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT birthdate FROM people");
            List<AbstractBucket> buckets = generateAgeDivisions();
            return generateResults(stmt, buckets);
        } catch (SQLException e) {
            throw new RuntimeException("Could generate age demographics", e);
        }
    }

    public MetricsResponse getMembershipLength() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT member_since FROM people");
            List<AbstractBucket> buckets = generateLongevityDivisions();
            return generateResults(stmt, buckets);
        } catch (SQLException e) {
            throw new RuntimeException("Could generate age demographics", e);
        }
    }

    public MetricsResponse getNewYearlyMembership() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT member_since FROM people");
            List<AbstractBucket> buckets = generateYearlyDivisions(20);
            return generateResults(stmt, buckets, true);
        } catch (SQLException e) {
            throw new RuntimeException("Could generate age demographics", e);
        }
    }

    public MetricsResponse getNewMonthlyMembership() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT member_since FROM people");
            List<AbstractBucket> buckets = generateMonthlyDivisions(24);
            return generateResults(stmt, buckets, true);
        } catch (SQLException e) {
            throw new RuntimeException("Could generate age demographics", e);
        }
    }


    // ----- Private -----
    class DateRangeBucket extends AbstractBucket {
        Date startDate;

        private DateRangeBucket(String description, Date startDate) {
            super(description);
            this.startDate = startDate;
        }

        @Override
        public boolean itemFits(ResultSet rs) throws SQLException {
            Date d = rs.getDate(1);
            if(d == null) return false;
            return startDate.before(d);
        }
    }


    private List<AbstractBucket> generateYearlyDivisions(int numberOfYears) {
        LinkedList<AbstractBucket> buckets = new LinkedList<>();

        LocalDate startOfYear = new LocalDate().withDayOfYear(1);
        buckets.add(new DateRangeBucket(Integer.toString(startOfYear.getYear()), startOfYear.toDate()));
        for(int i=1; i<numberOfYears; i++) {
            LocalDate date = startOfYear.plusYears(-i);
            buckets.add(new DateRangeBucket(date.toString("yyyy"), date.toDate()));
        }

        return buckets;
    }

    private List<AbstractBucket> generateMonthlyDivisions(int numberOfMonths) {
        LinkedList<AbstractBucket> buckets = new LinkedList<>();

        LocalDate startOfMonth = new LocalDate().withDayOfMonth(1);
        for(int i=0; i<numberOfMonths; i++) {
            LocalDate date = startOfMonth.plusMonths(-i);
            buckets.add(new DateRangeBucket(date.toString("MMMM yyyy"), date.toDate()));
        }

        return buckets;
    }

    private ArrayList<AbstractBucket> generateLongevityDivisions() {
        ArrayList<AbstractBucket> buckets = new ArrayList<>(8);

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

    private ArrayList<AbstractBucket> generateAgeDivisions() {
        ArrayList<AbstractBucket> buckets = new ArrayList<>(9);

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
