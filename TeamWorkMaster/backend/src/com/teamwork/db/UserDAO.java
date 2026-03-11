package com.teamwork.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // ==========================================
    // 1. HÀM ĐĂNG NHẬP (TRẢ VỀ QUYỀN - ROLE)
    // ==========================================
    public String login(String username, String password) {
        String sql = "SELECT Role, IsActive, FullName FROM TBL_USERS WHERE Username = ? AND PasswordHash = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Kiểm tra xem tài khoản có bị Admin khóa không (IsActive == false)
                if (!rs.getBoolean("IsActive")) {
                    return "BANNED";
                }
                System.out.println("[AUTH] Dang nhap thanh cong: " + rs.getString("FullName") + " | Quyen: "
                        + rs.getString("Role"));
                return rs.getString("Role"); // Trả về ADMIN hoặc MEMBER
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("[UserDAO] Loi dang nhap: " + e.getMessage());
        }
        return null; // Sai tài khoản hoặc mật khẩu
    }

    // ==========================================
    // 2. HÀM ĐĂNG KÝ (DÀNH CHO NGƯỜI DÙNG TỰ ĐĂNG KÝ)
    // ==========================================
    public boolean register(String username, String password, String fullName, String email) {
        String sql = "INSERT INTO TBL_USERS (Username, PasswordHash, FullName, Email, Role) VALUES (?, ?, ?, ?, 'MEMBER')";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, fullName);
            pstmt.setString(4, email);
            int rowsInserted = pstmt.executeUpdate();
            pstmt.close();
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Loi dang ky: " + e.getMessage());
        }
        return false;
    }

    // ==========================================
    // 3. HÀM QUÉT TRÙNG LẶP DỮ LIỆU
    // ==========================================
    private boolean isDuplicate(String column, String value, int excludeId) {
        String sql = "SELECT ID FROM TBL_USERS WHERE " + column + " = ?";
        if (excludeId > 0) {
            sql += " AND ID != ?";
        }
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, value);
            if (excludeId > 0) {
                pstmt.setInt(2, excludeId);
            }
            ResultSet rs = pstmt.executeQuery();
            boolean exists = rs.next();
            pstmt.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==========================================
    // 4. TẠO MỚI TÀI KHOẢN (BỞI ADMIN)
    // ==========================================
    public int addUserByAdmin(String username, String password, String fullName, String email) {
        if (isDuplicate("Username", username, -1))
            return -1; // -1: Trùng Username
        if (isDuplicate("Email", email, -1))
            return -2; // -2: Trùng Email

        String sql = "INSERT INTO TBL_USERS (Username, PasswordHash, FullName, Email, Role, IsActive) VALUES (?, ?, ?, ?, 'MEMBER', 1)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, fullName);
            pstmt.setString(4, email);

            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            return rowsAffected > 0 ? 1 : 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Lỗi tạo tài khoản: " + e.getMessage());
        }
        return 0;
    }

    // ==========================================
    // 5. CẬP NHẬT TÀI KHOẢN (BỞI ADMIN)
    // ==========================================
    // ==========================================
    // 5. CẬP NHẬT TÀI KHOẢN (BỞI ADMIN) - ĐÃ BỎ KIỂM TRA PASS CŨ
    // ==========================================
    public int updateUser(int id, String fullName, String email, String newPassword) {
        // Chỉ cần kiểm tra trùng Email với người khác
        if (isDuplicate("Email", email, id))
            return -2; // -2: Email đã bị người khác lấy

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            boolean isChangePassword = (newPassword != null && !newPassword.trim().isEmpty());

            String sql;
            if (isChangePassword) {
                sql = "UPDATE TBL_USERS SET FullName = ?, Email = ?, PasswordHash = ? WHERE ID = ?";
            } else {
                sql = "UPDATE TBL_USERS SET FullName = ?, Email = ? WHERE ID = ?";
            }

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);

            if (isChangePassword) {
                pstmt.setString(3, newPassword);
                pstmt.setInt(4, id);
            } else {
                pstmt.setInt(3, id);
            }

            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            return rowsAffected > 0 ? 1 : 0; // 1: Thành công, 0: Thất bại

        } catch (SQLException e) {
            System.err.println("[UserDAO] Lỗi cập nhật User: " + e.getMessage());
            return 0;
        }
    }

    // ==========================================
    // 6. LẤY DANH SÁCH TÀI KHOẢN
    // ==========================================
    public String getAllMembersJson() {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT ID, Username, FullName, Email, IsActive FROM TBL_USERS WHERE Role = 'MEMBER'";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            boolean first = true;
            while (rs.next()) {
                if (!first)
                    json.append(",");
                json.append("{");
                json.append("\"id\":").append(rs.getInt("ID")).append(",");
                json.append("\"username\":\"").append(rs.getString("Username")).append("\",");
                json.append("\"fullname\":\"").append(rs.getString("FullName")).append("\",");
                json.append("\"email\":\"").append(rs.getString("Email")).append("\",");
                json.append("\"isActive\":").append(rs.getBoolean("IsActive"));
                json.append("}");
                first = false;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("[UserDAO] Lỗi lấy danh sách: " + e.getMessage());
        }
        json.append("]");
        return json.toString();
    }
}