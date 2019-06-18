package org.servantscode.metrics.db;

import org.servantscode.commons.db.DBAccess;
import org.servantscode.metrics.MinistyEnrollmentStatistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MinistryMetricsDB extends DBAccess {

    public MinistyEnrollmentStatistics getMinistryEnrollments(int ministryId) {
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT count(1) AS members, leader, contact " +
                                                                "FROM ministry_enrollments e, ministry_roles r " +
                                                                "WHERE e.role_id=r.id AND e.ministry_id=? " +
                                                                "GROUP BY leader, contact")) {
            stmt.setInt(1, ministryId);

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
