package org.servantscode.metrics.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.metrics.MonthlyDonations;
import org.servantscode.metrics.PledgeMetricsResponse;
import org.servantscode.metrics.db.PledgeMetricsDB;

import javax.ws.rs.*;

import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/metrics/pledges")
public class PlegeMetricsSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(PlegeMetricsSvc.class);

    @GET @Path("/status") @Produces(APPLICATION_JSON)
    public PledgeMetricsResponse getPledgeMetrics() {
        verifyUserAccess("pledge.metrics");
        try {
            LOG.debug("Retrieving pledge metrics.");
            return new PledgeMetricsDB().getPledgeStatuses();
        } catch (Throwable t) {
            LOG.error("Failed to generate pledge metrics.", t);
            throw t;
        }
    }

    @GET @Path("/monthly") @Produces(APPLICATION_JSON)
    public List<MonthlyDonations> getMonthlyDonations(@QueryParam("months") @DefaultValue("13") int months) {
        verifyUserAccess("donation.metrics");
        try {
            LOG.debug("Retrieving monthly donation metrics.");
            return new PledgeMetricsDB().getMonthlyDonations(months);
        } catch (Throwable t) {
            LOG.error("Failed to generate monthly donation metrics.", t);
            throw t;
        }
    }

    @GET @Path("/monthly/fund/{fundId}") @Produces(APPLICATION_JSON)
    public List<MonthlyDonations> getMonthlyDonationsByFund(@PathParam("fundId") int fundId,
                                                            @QueryParam("months") @DefaultValue("13") int months) {
        verifyUserAccess("donation.metrics");
        try {
            LOG.debug("Retrieving monthly donation metrics.");
            return new PledgeMetricsDB().getMonthlyDonationsForFund(months, fundId);
        } catch (Throwable t) {
            LOG.error("Failed to generate monthly donation metrics.", t);
            throw t;
        }
    }
}
