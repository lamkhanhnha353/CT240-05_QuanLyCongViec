package com.teamwork.db;

import com.teamwork.model.Comment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    // Lấy danh sách bình luận CỦA ĐÚNG THẺ ĐÓ
    public List<Comment> getCommentsByTask(int taskId) {
        List<Comment> comments = new ArrayList<>();
        // 🟢 ĐÃ THÊM: Lấy thêm cột c.FileUrl từ Database
        String sql = "SELECT c.ID, u.FullName as UserName, c.Content, c.FileUrl, c.CreatedAt " +
                "FROM TBL_COMMENTS c " +
                "LEFT JOIN TBL_USERS u ON c.UserID = u.ID " +
                "WHERE c.TaskID = ? " +
                "ORDER BY c.CreatedAt ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("ID"));
                String user = rs.getString("UserName");
                if (user == null)
                    user = "Người dùng ẩn";
                String content = rs.getString("Content");

                // 🟢 Lấy link ảnh từ Database
                String fileUrl = rs.getString("FileUrl");

                String time = rs.getTimestamp("CreatedAt").toString();

                // Tạo đối tượng Comment như cũ
                Comment comment = new Comment(id, user, content, time);

                // 🟢 Gắn thêm FileUrl vào đối tượng (Yêu cầu model Comment phải có hàm
                // setFileUrl)
                comment.setFileUrl(fileUrl);

                comments.add(comment);
            }
        } catch (SQLException e) {
            System.err.println("[CommentDAO] Lỗi lấy bình luận: " + e.getMessage());
        }
        return comments;
    }

    // Lưu bình luận mới (🟢 ĐÃ CẬP NHẬT: Thêm tham số fileUrl)
    public boolean addComment(int taskId, int userId, String content, String fileUrl) {
        // Đã thêm cột FileUrl vào câu lệnh INSERT
        String sql = "INSERT INTO TBL_COMMENTS (TaskID, UserID, Content, FileUrl, CreatedAt) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            stmt.setString(3, content);

            // 🟢 Xử lý lưu FileUrl (Nếu người dùng chỉ chat chữ, không gửi ảnh thì lưu
            // NULL)
            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                stmt.setNull(4, Types.VARCHAR);
            } else {
                stmt.setString(4, fileUrl);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CommentDAO] Lỗi thêm bình luận: " + e.getMessage());
            return false;
        }
    }
}