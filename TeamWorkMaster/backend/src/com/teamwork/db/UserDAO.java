package com.teamwork.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    /**
     * Hàm kiểm tra thông tin Đăng nhập của người dùng.
     * Trả về true nếu đúng tài khoản & mật khẩu, ngược lại trả về false.
     */
    /**
     * Ham kiem tra thong tin Dang nhap.
     * Tra ve true neu trung khop Username va Password.
     */
    public boolean login(String username, String password) {
        // Kiem tra xem co tai khoan nao khop Username va PasswordHash hay khong
        String sql = "SELECT * FROM TBL_USERS WHERE Username = ? AND PasswordHash = ?";

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            // Neu rs.next() la true -> Co it nhat 1 dong du lieu -> Dang nhap dung
            if (rs.next()) {
                System.out.println("[AUTH] Dang nhap thanh cong! Xin chao: " + rs.getString("FullName") + " (Role: "
                        + rs.getString("Role") + ")");
                return true;
            } else {
                System.out.println("[AUTH] Dang nhap that bai do sai thong tin: " + username);
            }

            pstmt.close();
        } catch (SQLException e) {
            System.err.println("[UserDAO] Loi ket noi CSDL khi dang nhap: " + e.getMessage());
        }

        return false;
    }

    /**
     * Ham dang ky tai khoan moi.
     * Tra ve true neu dang ky thanh cong, false neu that bai (vi du: trung
     * Username/Email).
     */
    public boolean register(String username, String password, String fullName, String email) {
        // Su dung INSERT INTO de them vao CSDL, Quyen (Role) mac dinh se la MEMBER
        String sql = "INSERT INTO TBL_USERS (Username, PasswordHash, FullName, Email, Role) VALUES (?, ?, ?, ?, 'MEMBER')";

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, username);
            pstmt.setString(2, password); // Tuong lai se bam mat khau o day
            pstmt.setString(3, fullName);
            pstmt.setString(4, email);

            // executeUpdate() tra ve so dong bi anh huong (neu > 0 la thanh cong)
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("[AUTH] Dang ky thanh cong tai khoan moi: " + username);
                return true;
            }
            pstmt.close();
        } catch (SQLException e) {
            // Loi thuong gap la trung Username hoac Email (do ta set UNIQUE trong MySQL)
            System.err.println("[UserDAO] Loi dang ky (Co the trung tai khoan/email): " + e.getMessage());
        }
        return false;
    }

    // Hàm main để test thử ngay lập tức mà không cần mở Dashboard
    public static void main(String[] args) {
        UserDAO dao = new UserDAO();
        System.out.println("--- TEST DANG NHAp ---");
        // Test với tài khoản Admin chúng ta đã tạo trong MySQL lúc nãy
        dao.login("admin", "123456");

        System.out.println("----------------------");
        // Test với tài khoản sai
        dao.login("admin", "matkhausai");
    }
}