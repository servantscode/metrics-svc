package org.servantscode.metrics.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.metrics.DonationTierReport;
import org.servantscode.metrics.util.AbstractBucket;
import org.servantscode.metrics.util.Collector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DonationTierDB extends DBAccess {
    private static final Logger LOG = LogManager.getLogger(DonationTierDB.class);

    public DonationTierReport getDonationStats(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        QueryBuilder query = select("f.surname", "f.id", "sum(d.amount) as total_donations")
                            .from("donations d")
                .leftJoin("families f ON d.family_id=f.id")
                .where("date >= ? and date <= ?",  start, end).inOrg("d.org_id")
                .groupBy("f.id");
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery()) {

            List<DonationRangeBucket> buckets = generateDivisions();
            DonationCollector collector = new DonationCollector();
            while(rs.next()) {
                boolean found = false;
                for(AbstractBucket bucket: buckets) {
                    if(bucket.itemFits(rs)) {
                        bucket.increment();
                        found = true;
                        break;
                    }
                }

                if(!found)
                    LOG.warn(String.format("Could not find bucket for donations from family %s. Total donations: %.2f", rs.getString("surname"), rs.getFloat("total_donations")));

                collector.collect(rs);
            }

            DonationTierReport report = new DonationTierReport(collector.families, collector.total, start, end);
            for(DonationRangeBucket bucket: buckets)
                report.addData(bucket.getName(), bucket.getValue(), bucket.getDonationTotal());

            return report;
        } catch (SQLException e) {
            throw new RuntimeException("Could generate donation tier report.", e);
        }
    }

    public List<Integer> availableDonationYears() {
        QueryBuilder query = select("DISTINCT EXTRACT('year' FROM date)").from("donations").inOrg();
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery()){

            ArrayList<Integer> years = new ArrayList<>(10);
            while(rs.next())
                years.add(rs.getInt(1));

            years.sort(Comparator.comparingInt(a -> -a));
            return years;
        } catch (SQLException e) {
            throw new RuntimeException("Could get list of years with donations.", e);
        }
    }

    // ----- Private -----
    class DonationCollector implements Collector {
        float total = 0;
        int families = 0;

        @Override
        public void collect(ResultSet rs) throws SQLException {
            total += rs.getFloat("total_donations");
            families++;
        }
    }

    class DonationRangeBucket extends AbstractBucket {
        float start;
        float end;
        float donationTotal;

        private DonationRangeBucket( float start, float end) {
            super(String.format("$%.2f - $%.2f", start, end));
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean itemFits(ResultSet rs) throws SQLException {
            float d = rs.getFloat("total_donations");

            if(d >= start && d <= end) {
                donationTotal += d;
                return true;
            }

            return false;
        }

        float getDonationTotal() { return donationTotal; }
    }

    private ArrayList<DonationRangeBucket> generateDivisions() {
        ArrayList<DonationRangeBucket> buckets = new ArrayList<>(8);

        buckets.add(new DonationRangeBucket(0f, 0f));
        buckets.add(new DonationRangeBucket(0.01f, 25f));
        buckets.add(new DonationRangeBucket(25.01f, 50f));
        buckets.add(new DonationRangeBucket(50.01f, 75f));
        buckets.add(new DonationRangeBucket(75.01f, 100f));
        buckets.add(new DonationRangeBucket(100.01f, 200f));
        buckets.add(new DonationRangeBucket(200.01f, 300f));
        buckets.add(new DonationRangeBucket(300.01f, 400f));
        buckets.add(new DonationRangeBucket(400.01f, 500f));
        buckets.add(new DonationRangeBucket(500.01f, 600f));
        buckets.add(new DonationRangeBucket(600.01f, 700f));
        buckets.add(new DonationRangeBucket(700.01f, 800f));
        buckets.add(new DonationRangeBucket(800.01f, 900f));
        buckets.add(new DonationRangeBucket(900.01f, 1000f));
        buckets.add(new DonationRangeBucket(1000.01f, 1500f));
        buckets.add(new DonationRangeBucket(1500.01f, 2000f));
        buckets.add(new DonationRangeBucket(2000.01f, 2500f));
        buckets.add(new DonationRangeBucket(2500.01f, 3000f));
        buckets.add(new DonationRangeBucket(3000.01f, 3500f));
        buckets.add(new DonationRangeBucket(3500.01f, 4000f));
        buckets.add(new DonationRangeBucket(4000.01f, 4500f));
        buckets.add(new DonationRangeBucket(4500.01f, 5000f));
        buckets.add(new DonationRangeBucket(5000.01f, 10000f));
        buckets.add(new DonationRangeBucket(10000.01f, 15000f));
        buckets.add(new DonationRangeBucket(15000.01f, 20000f));
        buckets.add(new DonationRangeBucket(20000.01f, 100000000f));

        return buckets;
    }
}
