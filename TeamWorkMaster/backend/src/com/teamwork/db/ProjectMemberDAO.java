package com.teamwork.db;

import com.teamwork.core.GlobalExceptionHandler;
import java.sql.*;

public class ProjectMemberDAO {
    
    // Hàm cũ (Dùng ID)
    public boolean addOrUpdateMember(int projectId, int userId, String role) {
        String sql = "INSERT INTO TBL_PROJECT_MEMBERS (ProjectID, UserID, Role) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE Role = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, role);
            pstmt.setString(4, role);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            GlobalExceptionHandler.handle("ProjectMemberDAO", e);
            return false;
        }
    }

    // [MỚI] Hàm thêm thành viên bằng Email
    public boolean addMemberByEmail(int projectId, String email, String role) {
        // Dùng truy vấn lồng (Subquery): Lấy ID từ TBL_USERS có Email tương ứng, rồi Insert
        String sql = "INSERT INTO TBL_PROJECT_MEMBERS (ProjectID, UserID, Role) " +
                    "SELECT ?, ID, ? FROM TBL_USERS WHERE Email = ? " +
                    "ON DUPLICATE KEY UPDATE Role = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, projectId);
            pstmt.setString(2, role);
            pstmt.setString(3, email);
            pstmt.setString(4, role);
            
            // Nếu email không tồn tại, executeUpdate sẽ trả về 0
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            GlobalExceptionHandler.handle("ProjectMemberDAO", e);
            return false;
        }
    }

    public String getUserRole(int userId, int projectId) {
        String sql = "SELECT Role FROM TBL_PROJECT_MEMBERS WHERE UserID = ? AND ProjectID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, projectId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("Role") : null;
        } catch (SQLException e) { return null; }
    }
}