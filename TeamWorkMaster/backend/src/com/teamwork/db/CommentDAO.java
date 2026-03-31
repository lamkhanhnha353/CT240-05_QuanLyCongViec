package com.teamwork.db;

import com.teamwork.model.Comment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    public List<Comment> getCommentsByTask(int taskId) {
        List<Comment> comments = new ArrayList<>();
   
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

            
                String fileUrl = rs.getString("FileUrl");

                String time = rs.getTimestamp("CreatedAt").toString();

            
                Comment comment = new Comment(id, user, content, time);

          
           
                comment.setFileUrl(fileUrl);

                comments.add(comment);
            }
        } catch (SQLException e) {
            System.err.println("[CommentDAO] Lỗi lấy bình luận: " + e.getMessage());
        }
        return comments;
    }

    
    public boolean addComment(int taskId, int userId, String content, String fileUrl) {
       
        String sql = "INSERT INTO TBL_COMMENTS (TaskID, UserID, Content, FileUrl, CreatedAt) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            stmt.setString(3, content);

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

    public boolean updateComment(int commentId, int userId, String newContent) {
        String sql = "UPDATE TBL_COMMENTS SET Content = ? WHERE ID = ? AND UserID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newContent);
            stmt.setInt(2, commentId);
            stmt.setInt(3, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CommentDAO] Lỗi cập nhật bình luận: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteComment(int commentId, int userId) {
       
        String sql = "DELETE FROM TBL_COMMENTS WHERE ID = ? AND UserID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CommentDAO] Lỗi xóa bình luận: " + e.getMessage());
            return false;
        }
    }
}