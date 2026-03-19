package com.teamwork.db;

import com.teamwork.core.GlobalExceptionHandler;
import java.sql.*;

public class ActivityLogDAO {
    public void log(int projectId, int userId, String action) {
        String sql = "INSERT INTO TBL_ACTIVITY_LOGS (ProjectID, UserID, Action) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, action);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            GlobalExceptionHandler.handle("ActivityLogDAO", e);
        }
    }
}