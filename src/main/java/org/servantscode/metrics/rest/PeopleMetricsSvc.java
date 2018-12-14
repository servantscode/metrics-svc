package org.servantscode.metrics.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.metrics.MetricsResponse;
import org.servantscode.metrics.db.PeopleMetricsDB;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/metrics/people")
public class PeopleMetricsSvc {
    private static final Logger LOG = LogManager.getLogger(PeopleMetricsSvc.class);

    @GET @Path("/age") @Produces(APPLICATION_JSON)
    public MetricsResponse getAgeDemographics() {
        try {
            LOG.debug("Retrieving age demographics.");
            MetricsResponse resp = new PeopleMetricsDB().getAges();
            return resp;
        } catch (Throwable t) {
            LOG.error("Failed to generate metrics.", t);
            throw t;
        }
    }
}
