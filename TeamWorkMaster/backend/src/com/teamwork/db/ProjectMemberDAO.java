package com.teamwork.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProjectMemberDAO {

    public boolean inviteMember(int projectId, int userId, String role) {

        String sql = "INSERT INTO TBL_PROJECT_MEMBERS(ProjectID,UserID,Role) VALUES(?,?,?)";

        try {

            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, projectId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, role);

            int rows = pstmt.executeUpdate();
            pstmt.close();

            return rows > 0;

        } catch (SQLException e) {

            System.err.println("[ProjectMemberDAO] Loi moi thanh vien: " + e.getMessage());
        }

        return false;
    }

}