package org.servantscode.metrics.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.ReportListStreamingOutput;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.metrics.MonthlyDonations;
import org.servantscode.metrics.PledgeMetricsResponse;
import org.servantscode.metrics.db.PledgeMetricsDB;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.servantscode.commons.rest.AdditionalMediaTypes.TEXT_CSV;

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

    @GET @Path("/status/fund/{fundId}") @Produces(APPLICATION_JSON)
    public PledgeMetricsResponse getPledgeMetricsByFund(@PathParam("fundId") int fundId) {
        verifyUserAccess("pledge.metrics");
        try {
            LOG.debug("Retrieving pledge metrics.");
            return new PledgeMetricsDB().getPledgeStatusesForFund(fundId);
        } catch (Throwable t) {
            LOG.error("Failed to generate pledge metrics.", t);
            throw t;
        }
    }

    @GET @Path("/monthly") @Produces({APPLICATION_JSON, TEXT_PLAIN, TEXT_CSV})
    public Response getMonthlyDonations(@QueryParam("months") @DefaultValue("13") int months,
                                        @HeaderParam("Accept") String responseType) {

        boolean generateCSV = responseType.equals(TEXT_PLAIN) || responseType.equals("text/csv");
        verifyUserAccess(generateCSV? "donation.export": "donation.metrics");

        try {
            LOG.debug("Retrieving monthly donation metrics.");
            List<MonthlyDonations> donations = new PledgeMetricsDB().getMonthlyDonations(months);

            Object retVal = generateCSV? new ReportListStreamingOutput<>(donations): donations;
            return Response.ok(retVal).build();
        } catch (Throwable t) {
            LOG.error("Failed to generate monthly donation metrics.", t);
            throw t;
        }
    }

    @GET @Path("/monthly/fund/{fundId}") @Produces({APPLICATION_JSON, TEXT_PLAIN, TEXT_CSV})
    public Response getMonthlyDonationsByFund(@PathParam("fundId") int fundId,
                                              @QueryParam("months") @DefaultValue("13") int months,
                                              @HeaderParam("Accept") String responseType) {

        boolean generateCSV = responseType.equals(TEXT_PLAIN) || responseType.equals("text/csv");
        verifyUserAccess(generateCSV? "donation.export": "donation.metrics");

        try {
            LOG.debug("Retrieving monthly donation metrics.");
            List<MonthlyDonations> donations = new PledgeMetricsDB().getMonthlyDonationsForFund(months, fundId);

            Object retVal = generateCSV? new ReportListStreamingOutput<>(donations): donations;
            return Response.ok(retVal).build();
        } catch (Throwable t) {
            LOG.error("Failed to generate monthly donation metrics.", t);
            throw t;
        }
    }
}
