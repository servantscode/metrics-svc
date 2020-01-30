package org.servantscode.metrics.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.metrics.DonationTierReport;
import org.servantscode.metrics.db.DonationTierDB;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import java.time.LocalDate;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/metrics/donations")
public class DonationMetrics extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(DonationMetrics.class);

    private final DonationTierDB db;
    public DonationMetrics() {
        this.db = new DonationTierDB();
    }

    @GET @Path("/tiers") @Produces(APPLICATION_JSON)
    public DonationTierReport getDonationTiers() {
        return getDonationTiers(LocalDate.now().getYear()-1);
    }

    @GET @Path("/tiers/{year}") @Produces(APPLICATION_JSON)
    public DonationTierReport getDonationTiers(@PathParam("year") int year) {
        verifyUserAccess("donation.metrics");
        try {
            LOG.debug("Retrieving annual donation tier statistics.");
            return db.getDonationStats(year);
        } catch (Throwable t) {
            LOG.error("Failed to generate annual donation tier statistics.", t);
            throw t;
        }
    }

    @GET @Path("/available") @Produces(APPLICATION_JSON)
    public List<Integer> availableReports() {
        try {
            return db.availableDonationYears();
        } catch(Throwable t) {
            LOG.error("Could not find years with available donations.", t);
            throw t;
        }
    }
}
