package com.teamwork.db;

import java.sql.*;

public class ProjectChatDAO {

    // Lấy toàn bộ tin nhắn của 1 dự án
    public String getMessages(int projectId) {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT c.*, u.FullName FROM TBL_PROJECT_CHAT c JOIN TBL_USERS u ON c.UserID = u.ID WHERE c.ProjectID = ? ORDER BY c.CreatedAt ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, projectId);
            ResultSet rs = pstmt.executeQuery();
            boolean first = true;

            while (rs.next()) {
                if (!first)
                    json.append(",");
                json.append("{")
                        .append("\"id\":").append(rs.getInt("ID")).append(",")
                        .append("\"user\":\"").append(escapeJson(rs.getString("FullName"))).append("\",")
                        .append("\"content\":\"").append(escapeJson(rs.getString("Message"))).append("\",")
                        .append("\"fileUrl\":\"").append(escapeJson(rs.getString("FileUrl"))).append("\",")
                        .append("\"time\":\"").append(rs.getTimestamp("CreatedAt")).append("\"")
                        .append("}");
                first = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json.append("]").toString();
    }

    // Lưu tin nhắn mới (Có kèm File URL nếu có)
    public boolean addMessage(int projectId, int userId, String message, String fileUrl) {
        String sql = "INSERT INTO TBL_PROJECT_CHAT (ProjectID, UserID, Message, FileUrl) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, projectId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, message);

            // Nếu có link file từ Cloudinary thì lưu, không thì để NULL
            if (fileUrl != null && !fileUrl.trim().isEmpty()) {
                pstmt.setString(4, fileUrl);
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa (Thu hồi) tin nhắn
    public boolean deleteMessage(int messageId, int userId) {
        // Có kiểm tra UserID để đảm bảo chỉ người gửi mới có quyền xóa
        String sql = "DELETE FROM TBL_PROJECT_CHAT WHERE ID = ? AND UserID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, messageId);
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String escapeJson(String data) {
        if (data == null)
            return "";
        return data.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}