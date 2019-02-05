package org.servantscode.metrics.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.metrics.MetricsResponse;
import org.servantscode.metrics.db.FamilyMetricsDB;
import org.servantscode.metrics.db.PeopleMetricsDB;

import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.servantscode.commons.StringUtils.isEmpty;

@Path("/metrics/families")
public class FamilyMetricsSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(FamilyMetricsSvc.class);

    @GET @Path("/size") @Produces(APPLICATION_JSON)
    public MetricsResponse getAgeDemographics() {
        verifyUserAccess("family.metrics");
        try {
            LOG.debug("Retrieving family size demographics.");
            return new FamilyMetricsDB().getFamilySizes();
        } catch (Throwable t) {
            LOG.error("Failed to generate family size demographic metrics.", t);
            throw t;
        }
    }
}
