package com.teamwork.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.teamwork.utils.PasswordUtil; 

public class UserDAO {

    public String login(String username, String password) {
        String sql = "SELECT ID, Role, IsActive, FullName FROM TBL_USERS WHERE Username = ? AND PasswordHash = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, PasswordUtil.hashPassword(password)); 
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                if (!rs.getBoolean("IsActive"))
                    return "BANNED";
          
                return rs.getInt("ID") + "-" + rs.getString("Role") + "-" + rs.getString("FullName");
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("[UserDAO] Loi dang nhap: " + e.getMessage());
        }
        return null;
    }

    public boolean register(String username, String password, String fullName, String email) {
        String sql = "INSERT INTO TBL_USERS (Username, PasswordHash, FullName, Email, Role) VALUES (?, ?, ?, ?, 'MEMBER')";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, PasswordUtil.hashPassword(password)); // 🟢 Băm mật khẩu khi lưu
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

    private boolean isDuplicate(String column, String value, int excludeId) {
        String sql = "SELECT ID FROM TBL_USERS WHERE " + column + " = ?";
        if (excludeId > 0) {
            sql += " AND ID != ?";
        }
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, value);
            if (excludeId > 0)
                pstmt.setInt(2, excludeId);
            ResultSet rs = pstmt.executeQuery();
            boolean exists = rs.next();
            pstmt.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int addUserByAdmin(String username, String password, String fullName, String email) {
        if (isDuplicate("Username", username, -1))
            return -1;
        if (isDuplicate("Email", email, -1))
            return -2;

        String sql = "INSERT INTO TBL_USERS (Username, PasswordHash, FullName, Email, Role, IsActive) VALUES (?, ?, ?, ?, 'MEMBER', 1)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, PasswordUtil.hashPassword(password)); // 🟢 Băm mật khẩu khi Admin tạo mới
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

    public int updateUser(int id, String fullName, String email, String newPassword) {
        if (isDuplicate("Email", email, id))
            return -2;

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
                pstmt.setString(3, PasswordUtil.hashPassword(newPassword)); // 🟢 Băm mật khẩu mới cập nhật
                pstmt.setInt(4, id);
            } else {
                pstmt.setInt(3, id);
            }

            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            return rowsAffected > 0 ? 1 : 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Lỗi cập nhật User: " + e.getMessage());
            return 0;
        }
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM TBL_USERS WHERE ID = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Lỗi xóa User: " + e.getMessage());
            return false;
        }
    }

    public boolean toggleUserLock(int id) {
        String sql = "UPDATE TBL_USERS SET IsActive = CASE WHEN IsActive = 1 THEN 0 ELSE 1 END WHERE ID = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Lỗi khóa/mở khóa User: " + e.getMessage());
            return false;
        }
    }

    public String getAllMembersJson() {
        StringBuilder json = new StringBuilder("[");
        // Bổ sung lấy cột CreatedAt từ Database
        String sql = "SELECT ID, Username, FullName, Email, IsActive, CreatedAt FROM TBL_USERS WHERE Role = 'MEMBER' ORDER BY ID ASC";
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
                json.append("\"isActive\":").append(rs.getBoolean("IsActive")).append(",");

                // Xử lý lấy ngày tạo (Đề phòng database cũ bị null)
                String createdAt = rs.getString("CreatedAt");
                json.append("\"createdAt\":\"").append(createdAt != null ? createdAt : "").append("\"");

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

    // Hàm tìm kiếm người dùng (Auto-suggest)
    public String searchUsers(String keyword) {
        StringBuilder json = new StringBuilder("[");
      
        String sql = "SELECT ID, Username, Email, FullName FROM TBL_USERS WHERE (Username LIKE ? OR Email LIKE ?) AND IsActive = 1 LIMIT 5";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            // Thêm dấu % để tìm kiếm chứa từ khóa
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            boolean first = true;
            while (rs.next()) {
                if (!first)
                    json.append(",");
                json.append("{")
                        .append("\"id\":").append(rs.getInt("ID")).append(",")
                        .append("\"username\":\"").append(rs.getString("Username")).append("\",")
                        .append("\"email\":\"").append(rs.getString("Email")).append("\",")
                        .append("\"fullName\":\"").append(rs.getString("FullName")).append("\"")
                        .append("}");
                first = false;
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json.append("]").toString();
    }

    public String getFullNameById(int id) {
        String sql = "SELECT FullName FROM TBL_USERS WHERE ID = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("FullName");
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("[UserDAO] Lỗi lấy FullName: " + e.getMessage());
        }
        return "";
    }

   
    // HÀM ĐẶT LẠI MẬT KHẨU (FORGOT PASSWORD)
  
    public boolean resetPassword(String email, String newPassword) {
        // Chỉ cho phép lấy lại pass nếu Email tồn tại và tài khoản đang hoạt động
        // (IsActive = 1)
        String sql = "UPDATE TBL_USERS SET PasswordHash = ? WHERE Email = ? AND IsActive = 1";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, PasswordUtil.hashPassword(newPassword)); //  Băm mật khẩu khi đặt lại
            pstmt.setString(2, email);

            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();

            return rowsAffected > 0; // Trả về true nếu cập nhật thành công
        } catch (SQLException e) {
            System.err.println("[UserDAO] Lỗi reset mật khẩu: " + e.getMessage());
        }
        return false;
    }

    //  LẤY HỒ SƠ ĐỂ HIỂN THỊ EMAIL
    public String getUserProfile(int userId) {
        String sql = "SELECT Username, FullName, Email, Role FROM TBL_USERS WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String email = rs.getString("Email");
                return String.format("{\"username\":\"%s\", \"fullName\":\"%s\", \"email\":\"%s\", \"role\":\"%s\"}",
                        rs.getString("Username"), rs.getString("FullName"),
                        email != null ? email : "", rs.getString("Role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //  ĐỔI MẬT KHẨU
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        String checkSql = "SELECT ID FROM TBL_USERS WHERE ID = ? AND PasswordHash = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmtCheck = conn.prepareStatement(checkSql)) {
            pstmtCheck.setInt(1, userId);
            pstmtCheck.setString(2, PasswordUtil.hashPassword(oldPassword)); // Băm mật khẩu cũ để so khớp với
                                                                            
            ResultSet rs = pstmtCheck.executeQuery();

            if (rs.next()) { // Nếu pass cũ đúng
                String updateSql = "UPDATE TBL_USERS SET PasswordHash = ? WHERE ID = ?";
                try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateSql)) {
                    pstmtUpdate.setString(1, PasswordUtil.hashPassword(newPassword)); //  Băm mật khẩu mới khi đổi
                    pstmtUpdate.setInt(2, userId);
                    return pstmtUpdate.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Lỗi đổi mật khẩu: " + e.getMessage());
        }
        return false;
    }
}