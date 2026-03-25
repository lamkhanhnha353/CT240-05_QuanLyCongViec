package com.teamwork.db;

import java.sql.*;

public class NotificationDAO {

    public void createInviteNotification(int userId, int projectId, String projectName, String inviterName) {
        System.out.println("DEBUG: createInviteNotification called for userId=" + userId);
        String sql = "INSERT INTO TBL_NOTIFICATIONS (UserID, ProjectID, Title, Message) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, projectId);
            pstmt.setString(3, "Lời mời dự án mới");
            pstmt.setString(4, inviterName + " đã mời bạn tham gia dự án: " + projectName);
            int rows = pstmt.executeUpdate();
            System.out.println("DEBUG: Inserted " + rows + " notification rows");
        } catch (SQLException e) {
            System.err.println("DEBUG: Lỗi tạo notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Hàm gửi thông báo đa năng (Dùng cho mọi loại thông báo trong dự án)
    public void addNotification(int userId, int projectId, String title, String message) {
        String sql = "INSERT INTO TBL_NOTIFICATIONS (UserID, ProjectID, Title, Message) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, projectId);
            pstmt.setString(3, title);
            pstmt.setString(4, message);

            pstmt.executeUpdate();
            System.out.println("DEBUG: Đã tạo thông báo chung cho UserID=" + userId + ", ProjectID=" + projectId);
        } catch (SQLException e) {
            System.err.println("DEBUG: Lỗi tạo thông báo chung: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String escapeJson(String data) {
        if (data == null)
            return "";
        return data.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t");
    }

    public String getUserNotifications(int userId) {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT * FROM TBL_NOTIFICATIONS WHERE UserID = ? ORDER BY CreatedAt DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first)
                    json.append(",");
                json.append("{")
                        .append("\"id\":").append(rs.getInt("ID")).append(",")
                        .append("\"projectId\":").append(rs.getInt("ProjectID")).append(",")
                        .append("\"title\":\"").append(escapeJson(rs.getString("Title"))).append("\",")
                        .append("\"message\":\"").append(escapeJson(rs.getString("Message"))).append("\",")
                        .append("\"isRead\":").append(rs.getBoolean("IsRead"))
                        .append("}");
                first = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json.append("]").toString();
    }

    public void markAsRead(int notifId) {
        String sql = "UPDATE TBL_NOTIFICATIONS SET IsRead = TRUE WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notifId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteNotification(int notifId) {
        String sql = "DELETE FROM TBL_NOTIFICATIONS WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notifId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}