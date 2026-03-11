package com.teamwork.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object cho Project
 */
public class ProjectDAO {

    /**
     * Tạo project mới
     * @return 
     */
    public boolean createProject(String name, String description, int userId) {

    String sql = "INSERT INTO TBL_PROJECTS(ProjectName, Description, CreatedBy) VALUES(?,?,?)";

    try {

        Connection conn = DatabaseConnection.getInstance().getConnection();

        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setString(1, name);
        pstmt.setString(2, description);
        pstmt.setInt(3, userId);

        int rows = pstmt.executeUpdate();

        if (rows > 0) {
            System.out.println("[ProjectDAO] Tao project thanh cong!");
            return true;
        }

        pstmt.close();

    } catch (SQLException e) {

        System.err.println("[ProjectDAO] Loi tao project: " + e.getMessage());
    }

    return false;
}

    /**
     * Hiển thị danh sách project
     */
    public void listProjects() {

        String sql = "SELECT * FROM TBL_PROJECTS";

        try {

            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            ResultSet rs = pstmt.executeQuery();

            boolean empty = true;

            while (rs.next()) {

                empty = false;

                System.out.println(
                        "ProjectID=" + rs.getInt("ID")
                        + " | Name=" + rs.getString("ProjectName")
                        + " | Owner=" + rs.getInt("CreatedBy")
                        + " | Status=" + rs.getString("Status")
                );
            }

            if (empty) {
                System.out.println("Không có project nào trong database.");
            }

            pstmt.close();

        } catch (SQLException e) {
            System.err.println("[ProjectDAO] Lỗi list project: " + e.getMessage());
        }
    }

    /**
     * Cập nhật project
     */
    public void updateProject(int projectId, String newName, String newDescription) {

        String sql = "UPDATE TBL_PROJECTS SET ProjectName=?, Description=? WHERE ID=?";

        try {

            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, newName);
            pstmt.setString(2, newDescription);
            pstmt.setInt(3, projectId);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("[ProjectDAO] Cập nhật project thành công!");
            } else {
                System.out.println("[ProjectDAO] Không tìm thấy project để cập nhật.");
            }

            pstmt.close();

        } catch (SQLException e) {
            System.err.println("[ProjectDAO] Lỗi update project: " + e.getMessage());
        }
    }

    /**
     * Xóa project
     */
    public void deleteProject(int projectId) {

        String sql = "DELETE FROM TBL_PROJECTS WHERE ID=?";

        try {

            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, projectId);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("[ProjectDAO] Xóa project thành công!");
            } else {
                System.out.println("[ProjectDAO] Không tìm thấy project để xóa.");
            }

            pstmt.close();

        } catch (SQLException e) {
            System.err.println("[ProjectDAO] Lỗi delete project: " + e.getMessage());
        }
    }

    /**
     * Mời thành viên vào project
     */
    public void inviteMember(int projectId, int userId, String role) {

        String sql = "INSERT INTO TBL_PROJECT_MEMBERS(ProjectID, UserID, Role) VALUES(?,?,?)";

        try {

            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, projectId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, role);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("[ProjectDAO] Mời thành viên vào project thành công!");
            }

            pstmt.close();

        } catch (SQLException e) {
            System.err.println("[ProjectDAO] Lỗi mời thành viên: " + e.getMessage());
        }
    }
}
