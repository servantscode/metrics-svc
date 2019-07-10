package org.servantscode.metrics.db;

import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.metrics.MinistyEnrollmentStatistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MinistryMetricsDB extends DBAccess {

    public MinistyEnrollmentStatistics getMinistryEnrollments(int ministryId) {
        QueryBuilder query = select("count(1) AS members", "leader", "contact").from("ministry_enrollments e", "ministry_roles r")
                .where("e.role_id=r.id").where("e.ministry_id=?", ministryId).inOrg("r.org_id")
                .groupBy("leader", "contact");
//        conn.prepareStatement("SELECT count(1) AS members, leader, contact " +
//                                                                "FROM ministry_enrollments e, ministry_roles r " +
//                                                                "WHERE e.role_id=r.id AND e.ministry_id=? " +
//                                                                "GROUP BY leader, contact")
        try(Connection conn = getConnection();
            PreparedStatement stmt = query.prepareStatement(conn)) {

            try(ResultSet rs = stmt.executeQuery()) {
                MinistyEnrollmentStatistics stats = new MinistyEnrollmentStatistics();
                while(rs.next()) {
                    int members = rs.getInt("members");
                    stats.addMembers(members);

                    if(rs.getBoolean("leader"))
                        stats.addLeaders(members);

                    if(rs.getBoolean("contact"))
                        stats.addContacts(members);
                }
                return stats;
            }
        } catch(SQLException e) {
            throw new RuntimeException("Could not collect enrollment stats for ministry " + ministryId, e);
        }
    }
}
