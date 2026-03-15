package com.teamwork.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    // 1. LẤY DANH SÁCH CÔNG VIỆC
    public List<Task> getAllTasks() {
        List<Task> list = new ArrayList<>();
        // Gọi kết nối theo chuẩn Singleton
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        if (conn != null) {
            try {
                String sql = "SELECT * FROM tbl_tasks ORDER BY ID DESC";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("ID"));
                    task.setTitle(rs.getString("Title"));
                    task.setDescription(rs.getString("Description"));
                    task.setPriority(rs.getString("Priority"));
                    task.setDeadline(rs.getTimestamp("Deadline"));
                    task.setStatus(rs.getString("Status"));
                    
                    task.setAssigneeId(rs.getObject("AssigneeID") != null ? rs.getInt("AssigneeID") : null);
                    task.setProjectId(rs.getObject("ProjectID") != null ? rs.getInt("ProjectID") : null);
                    task.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    
                    list.add(task);
                }
                // Chỉ đóng ResultSet và PreparedStatement, giữ lại Connection (Singleton)
                rs.close(); 
                pstmt.close(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    // 2. THÊM CÔNG VIỆC MỚI
    public boolean insertTask(Task task) {
        boolean isSuccess = false;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        if (conn != null) {
            try {
                String sql = "INSERT INTO tbl_tasks (Title, Description, Priority, Deadline, Status, AssigneeID, ProjectID) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                
                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setString(3, task.getPriority() != null ? task.getPriority() : "Medium");
                pstmt.setTimestamp(4, task.getDeadline());
                pstmt.setString(5, task.getStatus() != null ? task.getStatus() : "Chưa bắt đầu");
                
                if (task.getAssigneeId() != null) pstmt.setInt(6, task.getAssigneeId());
                else pstmt.setNull(6, java.sql.Types.INTEGER);
                
                if (task.getProjectId() != null) pstmt.setInt(7, task.getProjectId());
                else pstmt.setNull(7, java.sql.Types.INTEGER);

                int rows = pstmt.executeUpdate();
                isSuccess = rows > 0;
                
                pstmt.close(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isSuccess;
    }

    // 3. CẬP NHẬT CÔNG VIỆC (UPDATE)
    public boolean updateTask(Task task) {
        boolean isSuccess = false;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        if (conn != null) {
            try {
                String sql = "UPDATE tbl_tasks SET Title = ?, Description = ?, Priority = ?, Deadline = ?, Status = ?, AssigneeID = ?, ProjectID = ? WHERE ID = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                
                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setString(3, task.getPriority());
                pstmt.setTimestamp(4, task.getDeadline());
                pstmt.setString(5, task.getStatus());
                
                if (task.getAssigneeId() != null) pstmt.setInt(6, task.getAssigneeId());
                else pstmt.setNull(6, java.sql.Types.INTEGER);
                
                if (task.getProjectId() != null) pstmt.setInt(7, task.getProjectId());
                else pstmt.setNull(7, java.sql.Types.INTEGER);
                
                pstmt.setInt(8, task.getId());

                int rows = pstmt.executeUpdate();
                isSuccess = rows > 0; 
                
                pstmt.close(); 
            } catch (Exception e) {
                System.err.println("Lỗi khi cập nhật công việc: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return isSuccess;
    }

    // 4. XÓA CÔNG VIỆC (DELETE)
    public boolean deleteTask(int id) {
        boolean isSuccess = false;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        if (conn != null) {
            try {
                String sql = "DELETE FROM tbl_tasks WHERE ID = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                
                pstmt.setInt(1, id); 

                int rows = pstmt.executeUpdate();
                isSuccess = rows > 0;
                
                pstmt.close(); 
            } catch (Exception e) {
                System.err.println("Lỗi khi xóa công việc: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return isSuccess;
    }
}