package com.teamwork.db;

import com.teamwork.model.Comment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    // Lấy danh sách bình luận CỦA ĐÚNG THẺ ĐÓ
    public List<Comment> getCommentsByTask(int taskId) {
        List<Comment> comments = new ArrayList<>();
        // Lấy Content, Thời gian và Tên người bình luận (Join với TBL_USERS)
        String sql = "SELECT c.ID, u.FullName as UserName, c.Content, c.CreatedAt " +
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
                String time = rs.getTimestamp("CreatedAt").toString();

                comments.add(new Comment(id, user, content, time));
            }
        } catch (SQLException e) {
            System.err.println("[CommentDAO] Lỗi lấy bình luận: " + e.getMessage());
        }
        return comments;
    }

    // Lưu bình luận mới (KHÔNG hardcode nữa)
    public boolean addComment(int taskId, int userId, String content) {
        String sql = "INSERT INTO TBL_COMMENTS (TaskID, UserID, Content, CreatedAt) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            stmt.setString(3, content);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CommentDAO] Lỗi thêm bình luận: " + e.getMessage());
            return false;
        }
    }
}