package com.teamwork.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public class AdminDAO {

    public String getDashboardSummary() {
        StringBuilder json = new StringBuilder("{");
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            int totalUsers = 0, totalProjects = 0, newUsers = 0, bannedUsers = 0;

            // 1. QUERY LẤY 4 CHỈ SỐ THỐNG KÊ (Dùng DATE_SUB để lấy user mới trong 7 ngày)
            String statSql = "SELECT " +
                    "(SELECT COUNT(*) FROM TBL_USERS) AS TotalUsers, " +
                    "(SELECT COUNT(*) FROM TBL_PROJECTS) AS TotalProjects, " +
                    "(SELECT COUNT(*) FROM TBL_USERS WHERE CreatedAt >= DATE_SUB(NOW(), INTERVAL 7 DAY)) AS NewUsers, "
                    +
                    "(SELECT COUNT(*) FROM TBL_USERS WHERE IsActive = 0) AS BannedUsers";

            try (PreparedStatement ps = conn.prepareStatement(statSql);
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalUsers = rs.getInt("TotalUsers");
                    totalProjects = rs.getInt("TotalProjects");
                    newUsers = rs.getInt("NewUsers");
                    bannedUsers = rs.getInt("BannedUsers");
                }
            }

            json.append("\"stats\": {")
                    .append("\"totalUsers\":").append(totalUsers).append(",")
                    .append("\"totalProjects\":").append(totalProjects).append(",")
                    .append("\"newUsers\":").append(newUsers).append(",")
                    .append("\"bannedUsers\":").append(bannedUsers)
                    .append("},");

            // 2. QUERY LẤY DANH SÁCH 5 NGƯỜI DÙNG MỚI ĐĂNG KÝ
            json.append("\"recentUsers\": [");
            String userSql = "SELECT ID, FullName, Username, Email, Role, IsActive, CreatedAt FROM TBL_USERS ORDER BY CreatedAt DESC LIMIT 5";

            try (PreparedStatement ps = conn.prepareStatement(userSql);
                    ResultSet rs = ps.executeQuery()) {
                boolean first = true;
                // Định dạng ngày giờ thân thiện với người Việt
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                while (rs.next()) {
                    if (!first)
                        json.append(",");

                    String status = rs.getBoolean("IsActive") ? "ACTIVE" : "BANNED";
                    String dateStr = rs.getTimestamp("CreatedAt") != null ? sdf.format(rs.getTimestamp("CreatedAt"))
                            : "Chưa rõ";

                    json.append("{")
                            .append("\"id\":").append(rs.getInt("ID")).append(",")
                            .append("\"name\":\"").append(escapeJson(rs.getString("FullName"))).append("\",")
                            .append("\"username\":\"").append(escapeJson(rs.getString("Username"))).append("\",")
                            .append("\"email\":\"").append(escapeJson(rs.getString("Email"))).append("\",")
                            .append("\"role\":\"").append(rs.getString("Role")).append("\",")
                            .append("\"status\":\"").append(status).append("\",")
                            .append("\"date\":\"").append(dateStr).append("\"")
                            .append("}");
                    first = false;
                }
            }
            json.append("]");

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Lỗi CSDL khi lấy dữ liệu Admin\"}";
        }
        return json.append("}").toString();
    }

    private String escapeJson(String data) {
        if (data == null)
            return "";
        return data.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}