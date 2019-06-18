package org.servantscode.metrics.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.metrics.MinistyEnrollmentStatistics;
import org.servantscode.metrics.db.MinistryMetricsDB;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/metrics/ministry")
public class MinistryMetricsSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(MinistryMetricsSvc.class);

    @GET @Path("/{ministryId}/membership") @Produces(APPLICATION_JSON)
    public MinistyEnrollmentStatistics getMinistryEnrollment(@PathParam("ministryId") int ministryId) {
        verifyUserAccess("ministry.metrics");
        try {
            LOG.debug("Retrieving ministry enrollments.");
            return new MinistryMetricsDB().getMinistryEnrollments(ministryId);
        } catch (Throwable t) {
            LOG.error("Failed to generate family size demographic metrics.", t);
            throw t;
        }
    }
}
