package com.teamwork.db;

import java.sql.*;
import com.teamwork.plugins.EmailService;

public class ProjectMemberDAO {

    public boolean addOrUpdateMember(int projectId, int userId, String role) {
        String sql = "INSERT INTO TBL_PROJECT_MEMBERS (ProjectID, UserID, Role, InviteStatus) VALUES (?, ?, ?, 'JOINED') "
                + "ON DUPLICATE KEY UPDATE Role = ?, InviteStatus = 'JOINED'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, role);
            pstmt.setString(4, role);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public String inviteMember(int projectId, String projectName, String identifier, String role, String inviterName) {
        int targetUserId = -1;
        String targetEmail = "";

        String normalized = identifier == null ? "" : identifier.trim();
        if (normalized.startsWith("@"))
            normalized = normalized.substring(1).trim();
        if (normalized.isEmpty())
            return "Vui lòng cung cấp Username hoặc Email để mời!";

        String sqlFindUser = "SELECT ID, Email FROM TBL_USERS WHERE Username = ? OR Email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmtFind = conn.prepareStatement(sqlFindUser)) {
            pstmtFind.setString(1, normalized);
            pstmtFind.setString(2, normalized);
            ResultSet rs = pstmtFind.executeQuery();
            if (rs.next()) {
                targetUserId = rs.getInt("ID");
                targetEmail = rs.getString("Email");
            } else {
                return "Không tìm thấy người dùng nào khớp với thông tin này!";
            }
        } catch (SQLException e) {
            return "Lỗi CSDL khi tìm người dùng.";
        }

        String sqlCheck = "SELECT InviteStatus FROM TBL_PROJECT_MEMBERS WHERE ProjectID = ? AND UserID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck)) {
            pstmtCheck.setInt(1, projectId);
            pstmtCheck.setInt(2, targetUserId);
            ResultSet rsCheck = pstmtCheck.executeQuery();
            if (rsCheck.next()) {
                String status = rsCheck.getString("InviteStatus");
                if ("JOINED".equals(status))
                    return "Người này đã là thành viên của dự án!";
                if ("PENDING".equals(status))
                    return "Người này đã được mời và đang chờ xác nhận!";
            }
        } catch (SQLException e) {
            return "Lỗi CSDL khi kiểm tra thành viên.";
        }

        String sqlInvite = "INSERT INTO TBL_PROJECT_MEMBERS (ProjectID, UserID, Role, InviteStatus) VALUES (?, ?, ?, 'PENDING') "
                + "ON DUPLICATE KEY UPDATE Role = ?, InviteStatus = 'PENDING'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmtInvite = conn.prepareStatement(sqlInvite)) {
            pstmtInvite.setInt(1, projectId);
            pstmtInvite.setInt(2, targetUserId);
            pstmtInvite.setString(3, role);
            pstmtInvite.setString(4, role);
            int rows = pstmtInvite.executeUpdate();

            if (rows > 0) {
                new NotificationDAO().createInviteNotification(targetUserId, projectId, projectName, inviterName);
                if (!targetEmail.isEmpty()) {
                    String subject = "Lời mời tham gia dự án: " + projectName;
                    String message = String.format("Chào bạn,\n\n%s đã mời bạn tham gia dự án '%s' với vai trò %s.\n"
                            + "Vui lòng đăng nhập vào hệ thống TeamWork Master để xem thông báo và xác nhận.\n\nTrân trọng,\nĐội ngũ TeamWork Master.",
                            inviterName, projectName, role);
                    EmailService.sendEmail(targetEmail, subject, message);
                }
                return "SUCCESS";
            }
        } catch (SQLException e) {
            return "Lỗi CSDL khi tạo lời mời.";
        }
        return "Lỗi không xác định khi tạo lời mời.";
    }

    public boolean respondToInvite(int userId, int projectId, boolean isAccept) {
        if (!isAccept) {
            String sql = "DELETE FROM TBL_PROJECT_MEMBERS WHERE UserID = ? AND ProjectID = ? AND InviteStatus = 'PENDING'";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, projectId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                return false;
            }
        }

        String sql = "UPDATE TBL_PROJECT_MEMBERS SET InviteStatus = 'JOINED' WHERE UserID = ? AND ProjectID = ? AND InviteStatus = 'PENDING'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, projectId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (isAccept
                    && ("42S22".equals(e.getSQLState()) || e.getMessage().contains("Unknown column 'InviteStatus'"))) {
                String sqlFallback = "UPDATE TBL_PROJECT_MEMBERS SET Role = Role WHERE UserID = ? AND ProjectID = ?";
                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                        PreparedStatement pstmt = conn.prepareStatement(sqlFallback)) {
                    pstmt.setInt(1, userId);
                    pstmt.setInt(2, projectId);
                    return pstmt.executeUpdate() > 0;
                } catch (SQLException e2) {
                    return false;
                }
            }
            return false;
        }
    }

    public String getUserRole(int userId, int projectId) {
        String sql = "SELECT Role FROM TBL_PROJECT_MEMBERS WHERE UserID = ? AND ProjectID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, projectId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("Role") : null;
        } catch (SQLException e) {
            return null;
        }
    }

    // --- HÀM LẤY DANH SÁCH THÀNH VIÊN ĐỂ GIAO VIỆC ---
    public String getProjectMembersJson(int projectId) {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT u.ID, u.FullName, u.Email, pm.Role FROM TBL_PROJECT_MEMBERS pm " +
                "INNER JOIN TBL_USERS u ON pm.UserID = u.ID " +
                "WHERE pm.ProjectID = ? AND pm.InviteStatus = 'JOINED'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectId);
            ResultSet rs = pstmt.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first)
                    json.append(",");
                json.append("{")
                        .append("\"id\":").append(rs.getInt("ID")).append(",")
                        .append("\"fullName\":\"").append(rs.getString("FullName")).append("\",")
                        .append("\"email\":\"").append(rs.getString("Email")).append("\",")
                        .append("\"role\":\"").append(rs.getString("Role")).append("\"")
                        .append("}");
                first = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json.append("]").toString();
    }

    // 1. Cập nhật quyền hạn của thành viên (Thăng cấp / Giáng cấp)
    public boolean updateRole(int projectId, int userId, String newRole) {
        String sql = "UPDATE TBL_PROJECT_MEMBERS SET Role = ? WHERE ProjectID = ? AND UserID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newRole);
            pstmt.setInt(2, projectId);
            pstmt.setInt(3, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. Xóa thành viên khỏi dự án (Kick hoặc Tự rời đi)
    public boolean removeMember(int projectId, int userId) {
        String sql = "DELETE FROM TBL_PROJECT_MEMBERS WHERE ProjectID = ? AND UserID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, projectId);
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}