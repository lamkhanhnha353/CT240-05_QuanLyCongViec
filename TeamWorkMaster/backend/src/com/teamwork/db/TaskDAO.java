package com.teamwork.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public List<Task> getAllTasks() {
        List<Task> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM TBL_TASKS ORDER BY ID DESC";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("ID"));
                    task.setTitle(rs.getString("Title"));
                    task.setDescription(rs.getString("Description"));
                    task.setPriority(rs.getString("Priority"));
                    task.setDeadline(rs.getDate("Deadline"));
                    task.setStatus(rs.getString("Status"));
                    
                    task.setAssigneeId(rs.getObject("AssigneeID") != null ? rs.getInt("AssigneeID") : null);
                    task.setProjectId(rs.getInt("ProjectID")); // Bắt buộc lấy ProjectID
                    task.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    list.add(task);
                }
                rs.close(); pstmt.close(); 
            } catch (Exception e) { e.printStackTrace(); }
        }
        return list;
    }

    public boolean insertTask(Task task) {
        boolean isSuccess = false;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "INSERT INTO TBL_TASKS (Title, Description, Priority, Deadline, Status, AssigneeID, ProjectID) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setString(3, task.getPriority() != null ? task.getPriority() : "MEDIUM");
                pstmt.setDate(4, task.getDeadline());
                pstmt.setString(5, task.getStatus() != null ? task.getStatus() : "TODO");
                
                if (task.getAssigneeId() != null) pstmt.setInt(6, task.getAssigneeId());
                else pstmt.setNull(6, java.sql.Types.INTEGER);
                
                pstmt.setInt(7, task.getProjectId()); // Bắt buộc truyền ProjectID

                isSuccess = pstmt.executeUpdate() > 0;
                pstmt.close(); 
            } catch (Exception e) { e.printStackTrace(); }
        }
        return isSuccess;
    }

    public boolean updateTask(Task task) {
        boolean isSuccess = false;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "UPDATE TBL_TASKS SET Title = ?, Description = ?, Priority = ?, Deadline = ?, Status = ?, AssigneeID = ?, ProjectID = ? WHERE ID = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setString(3, task.getPriority());
                pstmt.setDate(4, task.getDeadline());
                pstmt.setString(5, task.getStatus());
                
                if (task.getAssigneeId() != null) pstmt.setInt(6, task.getAssigneeId());
                else pstmt.setNull(6, java.sql.Types.INTEGER);
                
                pstmt.setInt(7, task.getProjectId());
                pstmt.setInt(8, task.getId());
                
                isSuccess = pstmt.executeUpdate() > 0; 
                pstmt.close(); 
            } catch (Exception e) { e.printStackTrace(); }
        }
        return isSuccess;
    }

    public boolean deleteTask(int id) {
        boolean isSuccess = false;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "DELETE FROM TBL_TASKS WHERE ID = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, id); 
                isSuccess = pstmt.executeUpdate() > 0;
                pstmt.close(); 
            } catch (Exception e) { e.printStackTrace(); }
        }
        return isSuccess;
    }
}