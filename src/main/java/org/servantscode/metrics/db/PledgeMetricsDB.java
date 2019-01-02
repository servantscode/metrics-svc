package org.servantscode.metrics.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.metrics.MonthlyDonations;
import org.servantscode.metrics.PledgeMetricsResponse;
import org.servantscode.metrics.util.AbstractBucket;
import org.servantscode.metrics.util.Collector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class PledgeMetricsDB extends AbstractMetricsDB {
    private static final Logger LOG = LogManager.getLogger(PledgeMetricsDB.class);

    public PledgeMetricsResponse getPledgeStatuses() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT COALESCE(d.family_id, p.family_id) AS family_id, d.total_donations, p.total_pledge " +
                            "FROM (SELECT family_id, SUM(amount) AS total_donations FROM donations WHERE date > ? GROUP BY family_id) d " +
                            "FULL OUTER JOIN (SELECT family_id, SUM(total_pledge) AS total_pledge FROM pledges WHERE pledge_start < NOW() AND pledge_end > NOW() GROUP BY family_id) p " +
                            "ON d.family_id=p.family_id");

            stmt.setDate(1, convert(LocalDate.now().withDayOfYear(1)));

            List<AbstractBucket> buckets = generateDivisions();
            PledgeCollector coll = new PledgeCollector();
            PledgeMetricsResponse resp = (PledgeMetricsResponse) generateResults(stmt, buckets, false, new PledgeMetricsResponse(), coll);
            resp.setDonationsToDate(coll.donations);
            resp.setTotalPledges(coll.pledges);
            return resp;
        } catch (SQLException e) {
            throw new RuntimeException("Could generate age demographics", e);
        }
    }

    public List<MonthlyDonations> getMonthlyDonations(int months) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT SUM(amount) AS total_donations, p.pledged, date_trunc('month', date) as month FROM donations d " +
                            "LEFT JOIN LATERAL (SELECT 't' as pledged FROM pledges WHERE family_id=d.family_id AND date < pledge_end AND date > pledge_start LIMIT 1) p " +
                            "ON TRUE " +
                            "WHERE date > date_trunc('month', NOW() - interval '" + months + " months') " +
                            "GROUP BY month, p.pledged ORDER BY month");

            return generateResults(stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Could generate age demographics", e);
        }
    }

    // ----- Private -----
    class PledgeCurrentBucket extends AbstractBucket {
        private int percentStart;
        private int percentEnd;

        private PledgeCurrentBucket (String description, int percentStart, int percentEnd) {
            super(description);
            this.percentEnd = percentEnd;
            this.percentStart = percentStart;
        }

        @Override
        public boolean itemFits(ResultSet rs) throws SQLException {
            float pledged = rs.getFloat("total_pledge");
            if(pledged == 0)
                return false;

            float donations = rs.getFloat("total_donations");
            int daysInYear = LocalDate.now().isLeapYear() ? 366: 365;
            float target = (pledged * LocalDate.now().getDayOfYear())/daysInYear;

            if(percentStart > 0 && donations < (target*percentStart)/100)
                return false;

            if(percentEnd > 0 && donations >= (target*percentEnd)/100)
                return false;

            return true;
        }
    }

    class PledgeCollector implements Collector {
        float donations = 0;
        float pledges = 0;

        @Override
        public void collect(ResultSet rs) throws SQLException {
            donations += rs.getFloat("total_donations");
            pledges += rs.getFloat("total_pledge");
        }
    }

    private List<AbstractBucket> generateDivisions() {
        LinkedList<AbstractBucket> buckets = new LinkedList<>();

        buckets.add(new PledgeCurrentBucket("Current", 100, 0));
        buckets.add(new PledgeCurrentBucket("Slightly behind", 85, 100));
        buckets.add(new PledgeCurrentBucket("Behind", 1, 85));
        buckets.add(new PledgeCurrentBucket("Unstarted", 0, 1));

        return buckets;
    }

    private List<MonthlyDonations> generateResults(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            HashMap<Date, MonthlyDonations> donations = new HashMap<>();

            while (rs.next()) {
                float amount = rs.getFloat("total_donations");
                boolean pledged = rs.getBoolean("pledged");
                Date monthStart = rs.getDate("month");

                MonthlyDonations monthly;
                if((monthly = donations.get(monthStart)) == null)
                    monthly = new MonthlyDonations(monthStart);

                if (pledged)
                    monthly.addPledged(amount);
                else
                    monthly.addUnpledged(amount);

                donations.put(monthStart, monthly);
            }

            ArrayList<MonthlyDonations> list = new ArrayList<>(donations.values());
            list.sort((a, b) -> b.getMonth().compareTo(a.getMonth())); //Descending order
            return list;
        }
    }
}
