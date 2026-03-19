package com.teamwork.db;

import java.sql.*;

public class ProjectDAO {

    // ĐÃ FIX: Đổi CreatedBy thành OwnerID
    public int createProject(String name, String description, String startDate, String endDate, int userId)
            throws Exception {
        String sql = "INSERT INTO TBL_PROJECTS (ProjectName, Description, StartDate, EndDate, Status, OwnerID) VALUES (?, ?, ?, ?, 'PENDING', ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, startDate);
            pstmt.setString(4, endDate);
            pstmt.setInt(5, userId); // Bắn userId vào cột OwnerID

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

    // ĐÃ FIX: Lấy OwnerID thay vì CreatedBy
    public String getAllProjectsJson() throws Exception {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT * FROM TBL_PROJECTS ORDER BY ID DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                if (json.length() > 1)
                    json.append(",");

                String desc = rs.getString("Description");
                if (desc != null)
                    desc = desc.replace("\"", "\\\"").replace("\n", "\\n");

                json.append(String.format(
                        "{\"id\":%d, \"name\":\"%s\", \"description\":\"%s\", \"status\":\"%s\", \"startDate\":\"%s\", \"endDate\":\"%s\", \"ownerId\":%d}",
                        rs.getInt("ID"),
                        rs.getString("ProjectName"),
                        desc,
                        rs.getString("Status"),
                        rs.getString("StartDate"),
                        rs.getString("EndDate"),
                        rs.getInt("OwnerID") // Trả về OwnerID
                ));
            }
        }
        return json.append("]").toString();
    }

    public boolean updateProject(int projectId, String name, String description, String status, String endDate)
            throws Exception {
        String sql = "UPDATE TBL_PROJECTS SET ProjectName = ?, Description = ?, Status = ?, EndDate = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, status);
            pstmt.setString(4, endDate);
            pstmt.setInt(5, projectId);
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
}