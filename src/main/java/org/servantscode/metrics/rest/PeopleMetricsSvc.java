package org.servantscode.metrics.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.metrics.MetricsResponse;
import org.servantscode.metrics.db.PeopleMetricsDB;

import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.servantscode.commons.StringUtils.isEmpty;

@Path("/metrics/people")
public class PeopleMetricsSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(PeopleMetricsSvc.class);

    @GET @Path("/age") @Produces(APPLICATION_JSON)
    public MetricsResponse getAgeDemographics() {
        verifyUserAccess("person.metrics");
        try {
            LOG.debug("Retrieving age demographics.");
            return new PeopleMetricsDB().getAges();
        } catch (Throwable t) {
            LOG.error("Failed to generate demographic metrics.", t);
            throw t;
        }
    }

    @GET @Path("/membership") @Produces(APPLICATION_JSON)
    public MetricsResponse getMembershipLongevity() {
        verifyUserAccess("person.metrics");
        try {
            LOG.debug("Retrieving membership longevity.");
            return new PeopleMetricsDB().getMembershipLength();
        } catch (Throwable t) {
            LOG.error("Failed to generate longevity metrics.", t);
            throw t;
        }
    }

    @GET @Path("/registration{timescale:(/(year|month))?}") @Produces(APPLICATION_JSON)
    public MetricsResponse getRegistrations(@PathParam("timescale") String timescale) {
        verifyUserAccess("person.metrics");
        if(isEmpty(timescale)) {
            timescale = "year";
        } else {
            timescale = timescale.substring(1);
        }

        try {
            LOG.debug("Retrieving new registrations by " + timescale);

            if(timescale.equalsIgnoreCase("year"))
                return new PeopleMetricsDB().getNewYearlyMembership();

            if(timescale.equalsIgnoreCase("month"))
                return new PeopleMetricsDB().getNewMonthlyMembership();

            throw new BadRequestException();
        } catch (Throwable t) {
            LOG.error("Failed to generate longevity metrics.", t);
            throw t;
        }
    }
}
