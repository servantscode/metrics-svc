package org.servantscode.metrics.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.metrics.MonthlyDonations;
import org.servantscode.metrics.PledgeMetricsResponse;
import org.servantscode.metrics.DonationReport;
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
    }

    public PledgeMetricsResponse getPledgeStatuses(int fundId) {
        ZonedDateTime start = ZonedDateTime.now().withMonth(1).withDayOfMonth(1).truncatedTo(DAYS);

        QueryBuilder joinedPledges = select("id", "fund_id", "total_pledge", "pledge_start", "pledge_end", "org_id").from("pledges")
                .where("pledge_end >= ?", convert(start)).inOrg();
        if(fundId > 0)
            joinedPledges.with("fund_id", fundId);

        QueryBuilder funds = select("COALESCE(d.fund_id, p.fund_id) AS fund_id", "family_id", "sum(amount) AS total_donations", "pledge_id", "total_pledge", "pledge_start", "pledge_end")
                .select("(now() <= pledge_end and now() >= pledge_start) AS active")
                .from("donations d")
                .fullOuterJoin(joinedPledges, "p", "d.pledge_id=p.id")
                .where("date >= ?", convert(start)).inOrg("d.org_id")
                .or().where("pledge_end >= ?", convert(start)).where("d.amount IS NULL").inOrg("p.org_id")
                .groupBy("d.pledge_id", "p.fund_id", "d.fund_id", "family_id", "pledge_start", "pledge_end", "total_pledge");

        QueryBuilder query = select("total_donations", "total_pledge", "pledge_start", "pledge_end")
                .select("total_donations/total_pledge AS collected_pct")
                //Ths can be better in postgres 12 when subtracting two dates gives you a number of days...
                //Alas... still on 11.5 in production
                //.select("(current_date - pledge_start)*1.0/(pledge_end - pledge_start) AS time_pct")
                .select("(EXTRACT(epoch from current_date) - EXTRACT(EPOCH FROM pledge_start))*1.0/(EXTRACT(EPOCH FROM pledge_end) - EXTRACT(EPOCH FROM pledge_start)) AS time_pct")
                .from(funds, "d");
        if(fundId > 0)
            query.with("fund_id", fundId);

        QueryBuilder finalQuery = select("*", "collected_pct - time_pct AS completion_score")
                .select("CASE WHEN total_pledge IS NULL THEN 'UNPLEDGED' " +
                        "WHEN total_donations >= total_pledge THEN 'COMPLETED' " +
                        "WHEN collected_pct - time_pct > -0.04 THEN 'CURRENT' " +
                        "WHEN collected_pct - time_pct > -0.082 THEN 'SLIGHTLY_BEHIND' " +
                        "WHEN total_donations > 0 THEN 'BEHIND' " +
                        "ELSE 'NOT_STARTED' END AS pledge_status")
                .from(query, "final");

        try (Connection conn = getConnection();
             PreparedStatement stmt = finalQuery.prepareStatement(conn)) {

            List<AbstractBucket> buckets = generateDivisions();
            PledgeCollector coll = new PledgeCollector(start.toLocalDate());
            PledgeMetricsResponse resp = (PledgeMetricsResponse) generateResults(stmt, buckets, false, new PledgeMetricsResponse(), coll);
            resp.setDonationsToDate(coll.donations);
            resp.setTotalPledges(coll.pledges);
            resp.setPledgedDonations(coll.pledgedDonations);
            resp.setUnpledgedDonations(coll.unpledgedDonations);
            resp.setPledgedTarget(coll.pledgedTarget);
            return resp;
        } catch (SQLException e) {
            throw new RuntimeException("Could generate pledge metrics", e);
        }
    }

    public DonationReport getDonationStats(LocalDate startDate, LocalDate endDate, int fundId) {
        QueryBuilder query = select("SUM(amount) AS total_donations", "pledge_id").from("donations")
                                .where("date >= ?", startDate).where("date <= ?", endDate).inOrg();
        if(fundId > 0)
            query.with("fund_id", fundId);
        query.groupBy("pledge_id");

        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery()) {

            DonationReport results = new DonationReport();

            results.setStartDate(startDate);
            results.setEndDate(endDate);
            results.setFundId(fundId);

            while(rs.next()) {
                int pledgeId = rs.getInt("pledge_id");
                float donations = rs.getFloat("total_donations");

                if(pledgeId > 0)
                    results.addPledged(donations);
                else
                    results.addUnpledged(donations);
            }

            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Could generate donation metrics.", e);
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
        private final String status;

//        private PledgeCurrentBucket (String description, float startScore, float endScore) {
        private PledgeCurrentBucket (String description, String status) {
            super(description);
            this.status = status;
        }

        @Override
        public boolean itemFits(ResultSet rs) throws SQLException {
            return this.status.equals(rs.getString("pledge_status"));
        }
    }

    class PledgeCollector implements Collector {
        float donations = 0;
        float pledges = 0;
        float pledgedDonations = 0;
        float unpledgedDonations = 0;
        float pledgedTarget = 0;

        LocalDate startDate;

        public PledgeCollector(LocalDate start) {
            this.startDate = start;
        }

        @Override
        public void collect(ResultSet rs) throws SQLException {
            float pledged = rs.getFloat("total_pledge");
            float total_donations = rs.getFloat("total_donations");
            donations += total_donations;
            pledges += pledged;
            if(pledged > 0) {
                LocalDate pledgeStart = convert(rs.getDate("pledge_start"));
                pledgeStart = pledgeStart.isBefore(startDate)? startDate: pledgeStart;

                LocalDate pledgeEnd = convert(rs.getDate("pledge_end"));
                LocalDate endOfYear = startDate.plusYears(1);
                pledgeEnd = pledgeEnd.isAfter(endOfYear)? endOfYear: pledgeEnd;

                long daysInPledge = DAYS.between(pledgeStart, pledgeEnd);
                long daysSinceStart = Math.min(DAYS.between(pledgeStart, LocalDate.now()), daysInPledge);

                if(daysSinceStart > 0)
                    pledgedTarget += (rs.getFloat("total_pledge") * ((daysSinceStart*1.0)/daysInPledge));

                pledgedDonations += Math.min(total_donations, pledged);
                unpledgedDonations += Math.max(0, total_donations - pledged);
            } else {
                unpledgedDonations += total_donations;
            }
        }
    }

    private List<AbstractBucket> generateDivisions() {
        LinkedList<AbstractBucket> buckets = new LinkedList<>();

        buckets.add(new PledgeCurrentBucket("Not Started", "NOT_STARTED"));
        buckets.add(new PledgeCurrentBucket("Behind", "BEHIND"));
        buckets.add(new PledgeCurrentBucket("Slightly behind", "SLIGHTLY_BEHIND"));
        buckets.add(new PledgeCurrentBucket("Current", "CURRENT"));
        buckets.add(new PledgeCurrentBucket("Completed", "COMPLETED"));
//        buckets.add(new PledgeCurrentBucket("Unpledged", "UNPLEDGED"));

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
