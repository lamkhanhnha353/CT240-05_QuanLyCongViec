package com.teamwork.db;

import java.sql.*;

public class TaskDAO {

    // 🟢 ĐÃ SỬA LẠI: Lấy danh sách công việc (Gom nhóm nhiều người làm bằng
    // GROUP_CONCAT)
    public String getTasksByProject(int projectId) {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT t.ID, t.Title, t.Description, t.Priority, t.Deadline, t.StartDate, t.Tags, t.Status, " +
                "GROUP_CONCAT(u.FullName SEPARATOR ', ') AS AssigneeNames, " +
                "GROUP_CONCAT(u.ID SEPARATOR ',') AS AssigneeIDs " +
                "FROM TBL_TASKS t " +
                "LEFT JOIN TBL_TASK_ASSIGNEES ta ON t.ID = ta.TaskID " +
                "LEFT JOIN TBL_USERS u ON ta.UserID = u.ID " +
                "WHERE t.ProjectID = ? " +
                "GROUP BY t.ID " +
                "ORDER BY t.OrderIndex ASC, t.CreatedAt DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectId);
            ResultSet rs = pstmt.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first)
                    json.append(",");

                String assigneeNames = rs.getString("AssigneeNames");
                String assigneeIds = rs.getString("AssigneeIDs");
                String tags = rs.getString("Tags");
                String startDate = rs.getString("StartDate");
                String deadline = rs.getString("Deadline");

                json.append("{")
                        .append("\"id\":").append(rs.getInt("ID")).append(",")
                        .append("\"title\":\"").append(escapeJson(rs.getString("Title"))).append("\",")
                        .append("\"description\":\"").append(escapeJson(rs.getString("Description"))).append("\",")
                        .append("\"priority\":\"").append(rs.getString("Priority")).append("\",")
                        .append("\"deadline\":\"").append(deadline != null ? deadline : "null").append("\",")
                        .append("\"startDate\":\"").append(startDate != null ? startDate : "null").append("\",")
                        .append("\"tags\":\"").append(tags != null ? escapeJson(tags) : "").append("\",")
                        .append("\"status\":\"").append(rs.getString("Status")).append("\",")
                        .append("\"assigneeName\":\"").append(assigneeNames != null ? escapeJson(assigneeNames) : "")
                        .append("\",")
                        .append("\"assigneeIds\":\"").append(assigneeIds != null ? assigneeIds : "").append("\"")
                        .append("}");
                first = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json.append("]").toString();
    }

    // 🟢 HÀM CREATE TASK LƯU NHIỀU NGƯỜI (GIỮ NGUYÊN BẢN CẬP NHẬT)
    public boolean createTask(int projectId, String title, String description, String priority, String deadline,
            String startDate, String tags, String status, String assigneeIdsStr) {

        String sqlTask = "INSERT INTO TBL_TASKS (ProjectID, Title, Description, Priority, Deadline, StartDate, Tags, Status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sqlTask, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, projectId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setString(4, priority);

            if (deadline == null || deadline.trim().isEmpty() || "null".equals(deadline))
                pstmt.setNull(5, java.sql.Types.DATE);
            else
                pstmt.setDate(5, java.sql.Date.valueOf(deadline));

            if (startDate == null || startDate.trim().isEmpty() || "null".equals(startDate))
                pstmt.setNull(6, java.sql.Types.DATE);
            else
                pstmt.setDate(6, java.sql.Date.valueOf(startDate));

            pstmt.setString(7, tags);
            pstmt.setString(8, status);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0)
                return false;

            int newTaskId = -1;
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next())
                    newTaskId = generatedKeys.getInt(1);
                else
                    return false;
            }

            if (assigneeIdsStr != null && !assigneeIdsStr.trim().isEmpty()) {
                String sqlAssign = "INSERT INTO TBL_TASK_ASSIGNEES (TaskID, UserID) VALUES (?, ?)";
                try (PreparedStatement pstmtAssign = conn.prepareStatement(sqlAssign)) {
                    String[] ids = assigneeIdsStr.split(",");
                    for (String idStr : ids) {
                        try {
                            int userId = Integer.parseInt(idStr.trim());
                            pstmtAssign.setInt(1, newTaskId);
                            pstmtAssign.setInt(2, userId);
                            pstmtAssign.addBatch();
                        } catch (NumberFormatException e) {
                        }
                    }
                    pstmtAssign.executeBatch();
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 🟢 KHÔNG ĐỔI
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

    // 🟢 KHÔNG ĐỔI
    public boolean deleteTask(int taskId) {
        String sql = "DELETE FROM TBL_TASKS WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 🟢 ĐÃ SỬA LẠI: Hỗ trợ Cập nhật nhiều người, Tags và StartDate 🟢
    public boolean updateTaskDetails(int taskId, String title, String description, String priority, String deadline,
            String startDate, String tags, String assigneeIdsStr) {

        String sql = "UPDATE TBL_TASKS SET Title = ?, Description = ?, Priority = ?, Deadline = ?, StartDate = ?, Tags = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, priority);

            if (deadline == null || deadline.trim().isEmpty() || "null".equals(deadline))
                pstmt.setNull(4, Types.DATE);
            else
                pstmt.setDate(4, Date.valueOf(deadline));

            if (startDate == null || startDate.trim().isEmpty() || "null".equals(startDate))
                pstmt.setNull(5, Types.DATE);
            else
                pstmt.setDate(5, Date.valueOf(startDate));

            pstmt.setString(6, tags);
            pstmt.setInt(7, taskId);

            pstmt.executeUpdate();

            // 1. Xóa sạch người thực hiện cũ của task này
            String sqlDel = "DELETE FROM TBL_TASK_ASSIGNEES WHERE TaskID = ?";
            try (PreparedStatement pDel = conn.prepareStatement(sqlDel)) {
                pDel.setInt(1, taskId);
                pDel.executeUpdate();
            }

            // 2. Chèn lại danh sách người thực hiện mới
            if (assigneeIdsStr != null && !assigneeIdsStr.trim().isEmpty()) {
                String sqlAssign = "INSERT INTO TBL_TASK_ASSIGNEES (TaskID, UserID) VALUES (?, ?)";
                try (PreparedStatement pAssign = conn.prepareStatement(sqlAssign)) {
                    String[] ids = assigneeIdsStr.split(",");
                    for (String idStr : ids) {
                        try {
                            int userId = Integer.parseInt(idStr.trim());
                            pAssign.setInt(1, taskId);
                            pAssign.setInt(2, userId);
                            pAssign.addBatch();
                        } catch (NumberFormatException e) {
                        }
                    }
                    pAssign.executeBatch();
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 🟢 KHÔNG ĐỔI
    // Lấy thống kê công việc CỦA RIÊNG 1 DỰ ÁN
    public int[] getTaskStatistics(int projectId) {
        int[] stats = new int[3]; // Vị trí 0: TODO, 1: IN_PROGRESS, 2: DONE
        String sql = "SELECT Status, COUNT(*) as Count FROM TBL_TASKS WHERE ProjectID = ? GROUP BY Status";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, projectId); // 🟢 Lọc theo Dự án
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String status = rs.getString("Status");
                int count = rs.getInt("Count");
                if ("TODO".equals(status))
                    stats[0] = count;
                else if ("IN_PROGRESS".equals(status))
                    stats[1] = count;
                else if ("DONE".equals(status))
                    stats[2] = count;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    // ==========================================
    // 🟢 CÁC HÀM XỬ LÝ CHECKLIST (SUBTASKS) 🟢
    // ==========================================

    public String getSubtasks(int taskId) {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT * FROM TBL_SUBTASKS WHERE TaskID = ? ORDER BY ID ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first)
                    json.append(",");
                json.append(String.format("{\"id\":%d,\"title\":\"%s\",\"isCompleted\":%b}",
                        rs.getInt("ID"), escapeJson(rs.getString("Title")), rs.getBoolean("IsCompleted")));
                first = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json.append("]").toString();
    }

    public boolean addSubtask(int taskId, String title) {
        String sql = "INSERT INTO TBL_SUBTASKS (TaskID, Title, IsCompleted) VALUES (?, ?, FALSE)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.setString(2, title);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean toggleSubtask(int subtaskId, boolean isCompleted) {
        String sql = "UPDATE TBL_SUBTASKS SET IsCompleted = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isCompleted);
            pstmt.setInt(2, subtaskId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSubtask(int subtaskId) {
        String sql = "DELETE FROM TBL_SUBTASKS WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subtaskId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==========================================
    // 🟢 HÀM XỬ LÝ LỊCH SỬ HOẠT ĐỘNG (TASK LOGS) 🟢
    // ==========================================
    public boolean addTaskLog(int taskId, int userId, String action) {
        String sql = "INSERT INTO TBL_TASK_LOGS (TaskID, UserID, Action) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, action);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public String getTaskLogs(int taskId) {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT l.*, u.FullName FROM TBL_TASK_LOGS l LEFT JOIN TBL_USERS u ON l.UserID = u.ID WHERE l.TaskID = ? ORDER BY l.CreatedAt DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first)
                    json.append(",");
                String user = rs.getString("FullName");
                if (user == null)
                    user = "Hệ thống";
                json.append(String.format("{\"id\":%d,\"user\":\"%s\",\"action\":\"%s\",\"time\":\"%s\"}",
                        rs.getInt("ID"), escapeJson(user), escapeJson(rs.getString("Action")),
                        rs.getTimestamp("CreatedAt").toString()));
                first = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json.append("]").toString();
    }

    public boolean updateTaskOrder(int taskId, int orderIndex) {
        String sql = "UPDATE TBL_TASKS SET OrderIndex = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderIndex);
            pstmt.setInt(2, taskId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ==========================================
    // 🟢 HÀM XỬ LÝ TÀI LIỆU ĐÍNH KÈM (ATTACHMENTS) 🟢
    // ==========================================
    public String getTaskAttachments(int taskId) {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT a.*, u.FullName FROM TBL_TASK_ATTACHMENTS a LEFT JOIN TBL_USERS u ON a.UserID = u.ID WHERE a.TaskID = ? ORDER BY a.CreatedAt DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first)
                    json.append(",");
                String user = rs.getString("FullName");
                json.append(String.format(
                        "{\"id\":%d,\"fileName\":\"%s\",\"fileUrl\":\"%s\",\"user\":\"%s\",\"time\":\"%s\"}",
                        rs.getInt("ID"), escapeJson(rs.getString("FileName")), escapeJson(rs.getString("FileUrl")),
                        escapeJson(user), rs.getTimestamp("CreatedAt").toString()));
                first = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json.append("]").toString();
    }

    public boolean addTaskAttachment(int taskId, int userId, String fileName, String fileUrl) {
        String sql = "INSERT INTO TBL_TASK_ATTACHMENTS (TaskID, UserID, FileName, FileUrl) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, fileName);
            pstmt.setString(4, fileUrl);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteTaskAttachment(int attachmentId) {
        String sql = "DELETE FROM TBL_TASK_ATTACHMENTS WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, attachmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private String escapeJson(String data) {
        if (data == null)
            return "";
        return data.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    // ==========================================
    // 🟢 HÀM MỚI: LẤY DANH SÁCH CÔNG VIỆC CỦA TÔI (MY TASKS) 🟢
    // ==========================================
    public String getMyTasks(int userId) {
        StringBuilder json = new StringBuilder("[");

        // Truy vấn lấy Task, join với Bảng Dự án để lấy tên,
        // và đếm số lượng Subtask (Checklist) để làm thanh Tiến độ
        String sql = "SELECT t.ID, t.Title, t.Description, t.Priority, t.Deadline AS EndDate, t.Status, " +
                "p.ProjectName, " +
                "(SELECT COUNT(*) FROM TBL_SUBTASKS st WHERE st.TaskID = t.ID) AS TotalTasks, " +
                "(SELECT COUNT(*) FROM TBL_SUBTASKS st WHERE st.TaskID = t.ID AND st.IsCompleted = 1) AS CompletedTasks "
                +
                "FROM TBL_TASKS t " +
                "JOIN TBL_PROJECTS p ON t.ProjectID = p.ID " +
                "JOIN TBL_TASK_ASSIGNEES ta ON t.ID = ta.TaskID " +
                "WHERE ta.UserID = ? " +
                "ORDER BY t.Deadline ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            boolean first = true;

            while (rs.next()) {
                if (!first)
                    json.append(",");

                String endDate = rs.getString("EndDate");

                json.append("{")
                        .append("\"id\":").append(rs.getInt("ID")).append(",")
                        .append("\"title\":\"").append(escapeJson(rs.getString("Title"))).append("\",")
                        .append("\"description\":\"").append(escapeJson(rs.getString("Description"))).append("\",")
                        .append("\"projectName\":\"").append(escapeJson(rs.getString("ProjectName"))).append("\",")
                        .append("\"priority\":\"").append(rs.getString("Priority")).append("\",")
                        .append("\"status\":\"").append(rs.getString("Status")).append("\",")
                        .append("\"endDate\":\"").append(endDate != null ? endDate : "null").append("\",")
                        .append("\"totalTasks\":").append(rs.getInt("TotalTasks")).append(",")
                        .append("\"completedTasks\":").append(rs.getInt("CompletedTasks"))
                        .append("}");
                first = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json.append("]").toString();
    }


    
}