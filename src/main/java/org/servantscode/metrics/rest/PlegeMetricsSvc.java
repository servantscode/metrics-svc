package org.servantscode.metrics.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.metrics.MonthlyDonations;
import org.servantscode.metrics.PledgeMetricsResponse;
import org.servantscode.metrics.db.PledgeMetricsDB;

import javax.ws.rs.*;

import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/metrics/pledges")
public class PlegeMetricsSvc {
    private static final Logger LOG = LogManager.getLogger(PlegeMetricsSvc.class);

    @GET @Path("/status") @Produces(APPLICATION_JSON)
    public PledgeMetricsResponse getPledgeMetrics() {
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
        try {
            LOG.debug("Retrieving monthly donation metrics.");
            return new PledgeMetricsDB().getMonthlyDonations(months);
        } catch (Throwable t) {
            LOG.error("Failed to generate monthly donation metrics.", t);
            throw t;
        }
    }
}
