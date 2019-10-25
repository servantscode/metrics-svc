package org.servantscode.metrics.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.metrics.MonthlyDonations;
import org.servantscode.metrics.PledgeMetricsResponse;
import org.servantscode.metrics.util.AbstractBucket;
import org.servantscode.metrics.util.Collector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

public class PledgeMetricsDB extends AbstractMetricsDB {
    private static final Logger LOG = LogManager.getLogger(PledgeMetricsDB.class);

    public PledgeMetricsResponse getPledgeStatuses() {
        return getPledgeStatuses(0);
//            PreparedStatement stmt = conn.prepareStatement("SELECT COALESCE(d.family_id, p.family_id) AS family_id, d.total_donations, p.total_pledge " +
//                            "FROM (SELECT family_id, SUM(amount) AS total_donations FROM donations WHERE date > ? AND org_id =? GROUP BY family_id) d " +
//                            "FULL OUTER JOIN (SELECT family_id, SUM(total_pledge) AS total_pledge FROM pledges WHERE pledge_start < NOW() AND pledge_end > NOW() AND org_id=? GROUP BY family_id) p " +
//                            "ON d.family_id=p.family_id");
    }

    public PledgeMetricsResponse getPledgeStatuses(int fundId) {
        QueryBuilder donationSelection = select("family_id", "fund_id", "SUM(amount) AS total_donations").from("donations")
                .where("date >= ?", convert(ZonedDateTime.now().withDayOfYear(1)))
                .inOrg().groupBy("family_id", "fund_id");

        QueryBuilder joinedPledges = select("family_id", "fund_id", "SUM(total_pledge) AS total_pledge", "pledge_start", "pledge_end").from("pledges")
                .where("pledge_start < NOW()").where("pledge_end > NOW()").inOrg();
        if(fundId > 0)
            joinedPledges.with("fund_id", fundId);
        joinedPledges.groupBy("family_id", "fund_id", "pledge_start", "pledge_end");

        QueryBuilder query = select("COALESCE(d.family_id, p.family_id) AS family_id", "d.total_donations", "p.total_pledge", "p.pledge_start", "p.pledge_end")
                .from(donationSelection, "d")
                .fullOuterJoin(joinedPledges, "p", "d.family_id=p.family_id AND d.fund_id=p.fund_id");

        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn)) {
//            PreparedStatement stmt = conn.prepareStatement("SELECT COALESCE(d.family_id, p.family_id) AS family_id, d.total_donations, p.total_pledge, p.pledge_start, p.pledge_end " +
//                    "FROM (SELECT family_id, SUM(amount) AS total_donations FROM donations WHERE date > ? AND org_id=? GROUP BY family_id) d " +
//                    "FULL OUTER JOIN (SELECT family_id, SUM(total_pledge) AS total_pledge FROM pledges WHERE fund_id=? AND pledge_start < NOW() AND pledge_end > NOW() AND org_id=? GROUP BY family_id) p " +
//                    "ON d.family_id=p.family_id");
//
//            stmt.setTimestamp(1, convert(ZonedDateTime.now().withDayOfYear(1)));
//            stmt.setInt(2, OrganizationContext.orgId());
//            stmt.setInt(3, fundId);
//            stmt.setInt(4, OrganizationContext.orgId());

            List<AbstractBucket> buckets = generateDivisions();
            PledgeCollector coll = new PledgeCollector();
            PledgeMetricsResponse resp = (PledgeMetricsResponse) generateResults(stmt, buckets, false, new PledgeMetricsResponse(), coll);
            resp.setDonationsToDate(coll.donations);
            resp.setTotalPledges(coll.pledges);
            return resp;
        } catch (SQLException e) {
            throw new RuntimeException("Could generate pledge metrics", e);
        }
    }

    public List<MonthlyDonations> getConsolodatedDonations(int windows, String timeWindow, int fundId) {
        int months = windows;
        if(timeWindow.equalsIgnoreCase("quarter"))
            months *= 3;
        if(timeWindow.equalsIgnoreCase("year"))
            months *= 12;

        QueryBuilder query = select("SUM(amount) AS total_donations", "p.pledged", String.format("date_trunc('%s', date) as time_window", timeWindow)).from("donations d")
                .leftJoinLateral(select("'t' AS pledged").from("pledges")
                                        .where("family_id=d.family_id").where("fund_id=d.fund_id").where("date <= pledge_end").where("date >= pledge_start")
                                        .inOrg().limit(1),
                           "p", "TRUE")
                .where(String.format("date > date_trunc('%s', NOW() - interval '%d months')", timeWindow, months)).inOrg();
        if(fundId > 0)
            query.with("fund_id", fundId);
        query.groupBy("time_window", "p.pledged").sort("time_window");
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery()) {

            return generateResults(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Could generate donation metrics.", e);
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

            LocalDate pledgeStart = convert(rs.getDate("pledge_start"));
            LocalDate pledgeEnd = convert(rs.getDate("pledge_end"));
            long daysInPledge = DAYS.between(pledgeStart, pledgeEnd);
            long daysSinceStart = DAYS.between(pledgeStart, LocalDate.now());

            float target = (pledged * daysSinceStart)/daysInPledge;

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

    private List<MonthlyDonations> generateResults(ResultSet rs) throws SQLException {
        HashMap<ZonedDateTime, MonthlyDonations> donations = new HashMap<>();

        while (rs.next()) {
            float amount = rs.getFloat("total_donations");
            boolean pledged = rs.getBoolean("pledged");
            ZonedDateTime monthStart = convert(rs.getTimestamp("time_window"));

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
