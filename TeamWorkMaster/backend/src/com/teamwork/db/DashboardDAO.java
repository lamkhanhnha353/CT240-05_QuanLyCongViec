package com.teamwork.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardDAO {

    public String getDashboardSummary(int userId) {
        StringBuilder json = new StringBuilder("{");
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            // 1. LẤY SỐ LIỆU STATS
            int todayTasks = 0, overdueTasks = 0, pendingTasks = 0, totalAssigned = 0, completedTasks = 0;

            String statsSql = "SELECT t.Deadline, t.Status FROM TBL_TASKS t " +
                    "JOIN TBL_TASK_ASSIGNEES ta ON t.ID = ta.TaskID WHERE ta.UserID = ?";
            try (PreparedStatement ps = conn.prepareStatement(statsSql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                long todayMillis = new java.util.Date().getTime();
                java.sql.Date today = new java.sql.Date(todayMillis);

                while (rs.next()) {
                    String status = rs.getString("Status");
                    java.sql.Date deadline = rs.getDate("Deadline");
                    totalAssigned++;

                    if ("DONE".equals(status)) {
                        completedTasks++;
                    } else if (!"CANCELED".equals(status)) {
                        pendingTasks++;
                        if (deadline != null) {
                            if (deadline.toString().equals(today.toString()))
                                todayTasks++;
                            else if (deadline.before(today))
                                overdueTasks++;
                        }
                    }
                }
            }
            int performance = (totalAssigned == 0) ? 0 : Math.round(((float) completedTasks / totalAssigned) * 100);

            json.append("\"stats\": {\"todayTasks\":").append(todayTasks)
                    .append(",\"overdueTasks\":").append(overdueTasks)
                    .append(",\"pendingTasks\":").append(pendingTasks)
                    .append(",\"performance\":").append(performance).append("},");

            // 2. LẤY VIỆC GẤP (URGENT TASKS)
            json.append("\"urgentTasks\": [");
            String urgentSql = "SELECT t.ID, t.Title, p.ProjectName, t.Deadline FROM TBL_TASKS t " +
                    "JOIN TBL_PROJECTS p ON t.ProjectID = p.ID " +
                    "JOIN TBL_TASK_ASSIGNEES ta ON t.ID = ta.TaskID " +
                    "WHERE ta.UserID = ? AND t.Status NOT IN ('DONE', 'CANCELED') AND t.Deadline IS NOT NULL " +
                    "ORDER BY t.Deadline ASC LIMIT 4";
            try (PreparedStatement ps = conn.prepareStatement(urgentSql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    json.append("{\"id\":").append(rs.getInt("ID"))
                            .append(",\"title\":\"").append(rs.getString("Title").replace("\"", "\\\""))
                            .append("\",\"project\":\"").append(rs.getString("ProjectName").replace("\"", "\\\""))
                            .append("\",\"timeLeft\":\"").append(rs.getDate("Deadline").toString()).append("\"}");
                    first = false;
                }
            }
            json.append("],");

            // 3. LẤY LỊCH SỬ HOẠT ĐỘNG (RECENT ACTIVITIES)
            json.append("\"activities\": [");
            String actSql = "SELECT l.Action, l.CreatedAt, u.FullName, t.Title FROM TBL_TASK_LOGS l " +
                    "JOIN TBL_USERS u ON l.UserID = u.ID " +
                    "JOIN TBL_TASKS t ON l.TaskID = t.ID " +
                    "ORDER BY l.CreatedAt DESC LIMIT 10";
            try (PreparedStatement ps = conn.prepareStatement(actSql)) {
                ResultSet rs = ps.executeQuery();
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    String name = rs.getString("FullName");
                    String avatar = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "⚙️";
                    json.append("{\"user\":\"").append(name != null ? name : "Hệ thống")
                            .append("\",\"action\":\"").append(rs.getString("Action").replace("\"", "\\\""))
                            .append("\",\"target\":\"").append(rs.getString("Title").replace("\"", "\\\""))
                            .append("\",\"time\":\"").append(rs.getTimestamp("CreatedAt").toString())
                            .append("\",\"avatar\":\"").append(avatar)
                            .append("\",\"color\":\"bg-blue-500\"}");
                    first = false;
                }
            }
            json.append("]");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.append("}").toString();
    }
}