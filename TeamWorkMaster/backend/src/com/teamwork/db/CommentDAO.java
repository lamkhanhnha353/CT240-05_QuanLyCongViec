package com.teamwork.db;

import com.teamwork.model.Comment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    /**
     * Hàm hỗ trợ: Tìm user_id dựa vào tên đăng nhập (username)
     */
    private int getUserIdByUsername(String username) {
        // Mình giả định bảng người dùng tên là 'tbl_users' và cột là 'username' (như
        // trong ảnh của bạn)
        String sql = "SELECT id FROM tbl_users WHERE username = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        if (conn != null) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id"); // Trả về ID thật của user
                    }
                }
            } catch (SQLException e) {
                System.err.println("[CommentDAO] Lỗi khi tra cứu user_id: " + e.getMessage());
            }
        }
        return 1; // Nếu lỗi hoặc không tìm thấy, tạm trả về 1 để không bị crash
    }

    /**
     * Lấy danh sách toàn bộ bình luận từ Database
     */
    public List<Comment> getAllComments() {
        List<Comment> comments = new ArrayList<>();

        // Dùng lệnh JOIN để nối bảng comments và tbl_users, lấy ra tên thật của người
        // dùng
        String sql = "SELECT c.id, u.username, c.content, c.created_at " +
                "FROM comments c " +
                "LEFT JOIN tbl_users u ON c.user_id = u.id " +
                "ORDER BY c.created_at ASC";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        if (conn == null) {
            System.err.println("[CommentDAO] Lỗi: Không thể lấy kết nối Database.");
            return comments;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id"));

                // Lấy tên thật từ bảng tbl_users (nếu không có thì để chữ Khách)
                String user = rs.getString("username");
                if (user == null)
                    user = "Người dùng ẩn";

                String content = rs.getString("content");
                String time = rs.getString("created_at");

                Comment comment = new Comment(id, user, content, time);
                comments.add(comment);
            }
        } catch (SQLException e) {
            System.err.println("[CommentDAO] Lỗi khi lấy bình luận (JOIN): " + e.getMessage());
        }
        return comments;
    }

    /**
     * Lưu một bình luận mới vào Database
     */
    public boolean addComment(Comment comment) {
        String sql = "INSERT INTO comments (task_id, user_id, content, created_at) VALUES (?, ?, ?, NOW())";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        if (conn == null)
            return false;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 1. Vẫn tạm giữ task_id = 3 vì đây là trang Tổng quan (chưa vào chi tiết từng
            // công việc)
            stmt.setInt(1, 3);

            // 2. Dịch từ tên đăng nhập (Vue gửi) sang ID thật
            int realUserId = getUserIdByUsername(comment.getUser());
            stmt.setInt(2, realUserId);

            // 3. Nội dung bình luận
            stmt.setString(3, comment.getContent());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.err.println("[CommentDAO] Lỗi khi thêm bình luận: " + e.getMessage());
            return false;
        }
    }
}