package com.teamwork.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    // 1. LẤY DANH SÁCH TẤT CẢ CÔNG VIỆC
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        // Sắp xếp công việc mới nhất lên đầu
        String sql = "SELECT * FROM tasks ORDER BY created_at DESC";

        // Giả sử class DatabaseConnection của bạn có hàm getConnection() trả về java.sql.Connection
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Task task = new Task(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("priority"),
                    rs.getTimestamp("deadline"),
                    // Xử lý cẩn thận vì assignee_id có thể mang giá trị NULL
                    rs.getObject("assignee_id") != null ? rs.getInt("assignee_id") : null,
                    rs.getTimestamp("created_at")
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // 2. THÊM MỚI CÔNG VIỆC
    public boolean insertTask(Task task) {
        String sql = "INSERT INTO tasks (title, description, status, priority, deadline, assignee_id) VALUES (?, ?, ?, ?, ?, ?)";

           try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getStatus() != null ? task.getStatus() : "To Do");
            stmt.setString(4, task.getPriority() != null ? task.getPriority() : "Medium");
            stmt.setTimestamp(5, task.getDeadline());

            // Xử lý người được giao việc (có thể chưa có ai nhận)
            if (task.getAssigneeId() != null) {
                stmt.setInt(6, task.getAssigneeId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 3. CẬP NHẬT CÔNG VIỆC
    public boolean updateTask(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, status = ?, priority = ?, deadline = ?, assignee_id = ? WHERE id = ?";
   try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getStatus());
            stmt.setString(4, task.getPriority());
            stmt.setTimestamp(5, task.getDeadline());

            if (task.getAssigneeId() != null) {
                stmt.setInt(6, task.getAssigneeId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            stmt.setInt(7, task.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 4. XÓA CÔNG VIỆC
    public boolean deleteTask(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
   try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}