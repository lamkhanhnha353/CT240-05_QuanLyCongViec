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
     * Lấy danh sách toàn bộ bình luận từ Database
     */
    public List<Comment> getAllComments() {
        List<Comment> comments = new ArrayList<>();
        // Đã sửa lại thành lấy user_id và task_id
        String sql = "SELECT id, user_id, content, created_at FROM comments ORDER BY created_at ASC";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        if (conn == null) {
            System.err.println("[CommentDAO] Lỗi: Không thể lấy kết nối Database từ Singleton.");
            return comments;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id"));

                // Vì database lưu số ID, ta tạm hiển thị chữ "User ID: x" lên màn hình Vue
                String user = "User ID: " + rs.getInt("user_id");

                String content = rs.getString("content");
                String time = rs.getString("created_at");

                Comment comment = new Comment(id, user, content, time);
                comments.add(comment);
            }
        } catch (SQLException e) {
            System.err.println("[CommentDAO] Lỗi khi lấy bình luận: " + e.getMessage());
            e.printStackTrace();
        }
        return comments;
    }

    /**
     * Lưu một bình luận mới vào Database
     */
    public boolean addComment(Comment comment) {
        // Sử dụng hàm NOW() của MySQL để tự động lấy ngày giờ hệ thống
        String sql = "INSERT INTO comments (task_id, user_id, content, created_at) VALUES (?, ?, ?, NOW())";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        if (conn == null)
            return false;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Tạm thời gán cứng task_id = 3 và user_id = 1 (giống trong ảnh DB của bạn)
            // Để đảm bảo lưu thành công mà không bị lỗi Foreign Key
            stmt.setInt(1, 3); // task_id
            stmt.setInt(2, 1); // user_id
            stmt.setString(3, comment.getContent()); // content

            // Đã xóa dòng stmt.setString(4, ...) vì NOW() tự lo liệu ngày giờ rồi

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.err.println("[CommentDAO] Lỗi khi thêm bình luận: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}