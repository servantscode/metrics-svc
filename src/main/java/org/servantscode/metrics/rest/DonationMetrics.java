package org.servantscode.metrics.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.metrics.DonationTierReport;
import org.servantscode.metrics.db.DonationTierDB;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/metrics/donations")
public class DonationMetrics extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(DonationMetrics.class);

    @GET @Path("/tiers") @Produces(APPLICATION_JSON)
    public DonationTierReport getDonationTiers() {
        verifyUserAccess("person.metrics");
        try {
            LOG.debug("Retrieving annual donation tier statistics.");
            return new DonationTierDB().getDonationStats();
        } catch (Throwable t) {
            LOG.error("Failed to generate annual donation tier statistics.", t);
            throw t;
        }
    }
}
