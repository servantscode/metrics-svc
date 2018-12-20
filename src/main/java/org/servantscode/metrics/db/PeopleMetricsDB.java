package org.servantscode.metrics.db;

import org.servantscode.metrics.MetricsResponse;
import org.servantscode.metrics.util.AbstractBucket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
        LocalDate startDate;

        private DateRangeBucket(String description, LocalDate startDate) {
            super(description);
            this.startDate = startDate;
        }

        @Override
        public boolean itemFits(ResultSet rs) throws SQLException {
            Date d = rs.getDate(1);
            if(d == null)
                return false;

            LocalDate date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return startDate.isBefore(date);
        }
    }

    private static final DateTimeFormatter yearFormat = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("MMM yyyy");

    private List<AbstractBucket> generateYearlyDivisions(int numberOfYears) {
        LinkedList<AbstractBucket> buckets = new LinkedList<>();

        LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
        buckets.add(new DateRangeBucket(startOfYear.format(yearFormat), startOfYear));
        for(int i=1; i<numberOfYears; i++) {
            LocalDate date = startOfYear.plusYears(-i);
            buckets.add(new DateRangeBucket(date.format(yearFormat), date));
        }

        return buckets;
    }

    private List<AbstractBucket> generateMonthlyDivisions(int numberOfMonths) {
        LinkedList<AbstractBucket> buckets = new LinkedList<>();

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        for(int i=0; i<numberOfMonths; i++) {
            LocalDate date = startOfMonth.plusMonths(-i);
            buckets.add(new DateRangeBucket(date.format(monthFormat), date));
        }

        return buckets;
    }

    private ArrayList<AbstractBucket> generateLongevityDivisions() {
        ArrayList<AbstractBucket> buckets = new ArrayList<>(8);

        LocalDate now = LocalDate.now();
        buckets.add(new DateRangeBucket("<1", now.plusYears(-1)));
        buckets.add(new DateRangeBucket("1-4", now.plusYears(-5)));
        buckets.add(new DateRangeBucket("5-9", now.plusYears(-10)));
        buckets.add(new DateRangeBucket("10-19", now.plusYears(-20)));
        buckets.add(new DateRangeBucket("20-29", now.plusYears(-30)));
        buckets.add(new DateRangeBucket("30-39", now.plusYears(-40)));
        buckets.add(new DateRangeBucket("40-49", now.plusYears(-50)));
        buckets.add(new DateRangeBucket("50+", now.plusYears(-MAX_AGE)));

        return buckets;
    }

    private ArrayList<AbstractBucket> generateAgeDivisions() {
        ArrayList<AbstractBucket> buckets = new ArrayList<>(9);

        LocalDate now = LocalDate.now();
        buckets.add(new DateRangeBucket("0-6", now.plusYears(-7)));
        buckets.add(new DateRangeBucket("7-11", now.plusYears(-12)));
        buckets.add(new DateRangeBucket("12-17", now.plusYears(-18)));
        buckets.add(new DateRangeBucket("18-24", now.plusYears(-25)));
        buckets.add(new DateRangeBucket("25-34", now.plusYears(-35)));
        buckets.add(new DateRangeBucket("35-44", now.plusYears(-45)));
        buckets.add(new DateRangeBucket("45-54", now.plusYears(-55)));
        buckets.add(new DateRangeBucket("55-64", now.plusYears(-65)));
        buckets.add(new DateRangeBucket("65+", now.plusYears(-MAX_AGE)));

        return buckets;
    }
}
