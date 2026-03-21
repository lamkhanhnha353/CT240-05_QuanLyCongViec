package com.teamwork.db;

import com.teamwork.core.GlobalExceptionHandler;
import java.sql.*;

public class StatusDAO {
    public void createDefaultStatus(int projectId) {
        String sql = "INSERT INTO TBL_STATUS (ProjectID, StatusName) VALUES (?, ?)";
        String[] defaults = {"To Do", "In Progress", "Done"};

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String s : defaults) {
                pstmt.setInt(1, projectId);
                pstmt.setString(2, s);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            GlobalExceptionHandler.handle("StatusDAO", e);
        }
    }
}