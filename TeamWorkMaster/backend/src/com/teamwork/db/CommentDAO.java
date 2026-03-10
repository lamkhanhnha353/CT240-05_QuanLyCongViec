package com.teamwork.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CommentDAO {

    // Thêm bình luận vào công việc
    public void addComment(int taskId, int userId, String content) {

        String sql = "INSERT INTO TBL_COMMENTS(TaskID, UserID, Content) VALUES (?, ?, ?)";

        try {

            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            ps.setString(3, content);

            ps.executeUpdate();

            System.out.println("Da them binh luan");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lấy danh sách bình luận theo Task
    public void getCommentsByTask(int taskId) {

        String sql = "SELECT * FROM TBL_COMMENTS WHERE TaskID = ?";

        try {

            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, taskId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                System.out.println(
                        rs.getInt("UserID") + " : " +
                                rs.getString("Content"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}