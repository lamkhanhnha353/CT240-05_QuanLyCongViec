package com.teamwork.db;

import java.sql.*;

public class TaskDAO {

    // Lấy toàn bộ công việc của 1 dự án
    public String getTasksByProject(int projectId) {
        StringBuilder json = new StringBuilder("[");
        // JOIN với bảng USERS để lấy ra tên người được giao việc (Assignee)
        String sql = "SELECT t.*, u.FullName as AssigneeName, u.Email as AssigneeEmail " +
                "FROM TBL_TASKS t LEFT JOIN TBL_USERS u ON t.AssigneeID = u.ID " +
                "WHERE t.ProjectID = ? ORDER BY t.CreatedAt DESC";
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
                        .append("\"title\":\"").append(escapeJson(rs.getString("Title"))).append("\",")
                        .append("\"description\":\"").append(escapeJson(rs.getString("Description"))).append("\",")
                        .append("\"priority\":\"").append(rs.getString("Priority")).append("\",")
                        .append("\"deadline\":\"").append(rs.getString("Deadline")).append("\",")
                        .append("\"status\":\"").append(rs.getString("Status")).append("\",") // TODO, IN_PROGRESS, DONE
                        .append("\"assigneeId\":").append(rs.getInt("AssigneeID")).append(",")
                        .append("\"assigneeName\":\"").append(escapeJson(rs.getString("AssigneeName"))).append("\"")
                        .append("}");
                first = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json.append("]").toString();
    }

    // Tạo công việc mới
    public boolean createTask(int projectId, String title, String description, String priority, String deadline,
            int assigneeId) {
        String sql = "INSERT INTO TBL_TASKS (ProjectID, Title, Description, Priority, Deadline, AssigneeID, Status) VALUES (?, ?, ?, ?, ?, ?, 'TODO')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setString(4, priority);

            if (deadline == null || deadline.trim().isEmpty() || deadline.equals("null")) {
                pstmt.setNull(5, Types.DATE);
            } else {
                pstmt.setDate(5, Date.valueOf(deadline));
            }

            if (assigneeId <= 0) {
                pstmt.setNull(6, Types.INTEGER); // Không giao cho ai (Unassigned)
            } else {
                pstmt.setInt(6, assigneeId);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật trạng thái (API siêu quan trọng dùng khi Kéo - Thả)
    public boolean updateTaskStatus(int taskId, String status) {
        String sql = "UPDATE TBL_TASKS SET Status = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, taskId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm chống lỗi font khi gửi JSON
    private String escapeJson(String data) {
        if (data == null)
            return "";
        return data.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}