package com.teamwork.db;

import java.sql.*;

public class ProjectDAO {

    public int createProject(String name, String description, String startDate, String endDate, String priority,
            int userId) throws Exception {
        String sql = "INSERT INTO TBL_PROJECTS (ProjectName, Description, StartDate, EndDate, Priority, Status, OwnerID) VALUES (?, ?, ?, ?, ?, 'PENDING', ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, startDate);
            pstmt.setString(4, endDate);
            pstmt.setString(5, priority);
            pstmt.setInt(6, userId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next())
                        return rs.getInt(1);
                }
            }
            return -1;
        }
    }

    public String getAllProjectsJson(int currentUserId) throws Exception {
        StringBuilder json = new StringBuilder("[");
        // SỬA LỖI BẢO MẬT: Dùng INNER JOIN để chỉ lấy dự án mà User này có mặt trong
        // TBL_PROJECT_MEMBERS
        // Dùng Subquery để đếm Tổng số Task và Số Task đã Done
        String sql = "SELECT p.*, pm.Role as MyRole, " +
                "(SELECT COUNT(*) FROM TBL_TASKS WHERE ProjectID = p.ID) as TotalTasks, " +
                "(SELECT COUNT(*) FROM TBL_TASKS WHERE ProjectID = p.ID AND Status = 'DONE') as CompletedTasks " +
                "FROM TBL_PROJECTS p " +
                "INNER JOIN TBL_PROJECT_MEMBERS pm ON p.ID = pm.ProjectID " +
                "WHERE pm.UserID = ? ORDER BY p.ID DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first)
                    json.append(",");
                String desc = rs.getString("Description");
                if (desc != null)
                    desc = desc.replace("\"", "\\\"").replace("\n", "\\n");

                json.append(String.format(
                        "{\"id\":%d, \"name\":\"%s\", \"description\":\"%s\", \"status\":\"%s\", \"priority\":\"%s\", \"startDate\":\"%s\", \"endDate\":\"%s\", \"ownerId\":%d, \"myRole\":\"%s\", \"totalTasks\":%d, \"completedTasks\":%d}",
                        rs.getInt("ID"), rs.getString("ProjectName"), desc, rs.getString("Status"),
                        rs.getString("Priority") != null ? rs.getString("Priority") : "MEDIUM",
                        rs.getString("StartDate"), rs.getString("EndDate"), rs.getInt("OwnerID"),
                        rs.getString("MyRole"), rs.getInt("TotalTasks"), rs.getInt("CompletedTasks")));
                first = false;
            }
        }
        return json.append("]").toString();
    }

    public boolean updateProject(int projectId, String name, String description, String status, String endDate,
            String priority) throws Exception {
        String sql = "UPDATE TBL_PROJECTS SET ProjectName = ?, Description = ?, Status = ?, EndDate = ?, Priority = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, status);
            pstmt.setString(4, endDate);
            pstmt.setString(5, priority);
            pstmt.setInt(6, projectId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteProject(int projectId) throws Exception {
        String sql = "DELETE FROM TBL_PROJECTS WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Integer getOwnerId(int projectId) throws Exception {
        String sql = "SELECT OwnerID FROM TBL_PROJECTS WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("OwnerID");
            }
        }
        return null;
    }
}