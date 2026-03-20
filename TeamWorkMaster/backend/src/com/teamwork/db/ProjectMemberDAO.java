package com.teamwork.db;

import java.sql.*;
import com.teamwork.plugins.EmailService; // Import thư viện gửi mail của đồng đội

public class ProjectMemberDAO {

    public boolean addOrUpdateMember(int projectId, int userId, String role) {
        String sql = "INSERT INTO TBL_PROJECT_MEMBERS (ProjectID, UserID, Role, InviteStatus) VALUES (?, ?, ?, 'JOINED') "
                +
                "ON DUPLICATE KEY UPDATE Role = ?, InviteStatus = 'JOINED'";
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

    // HÀM MỚI: Mời thành viên đa năng
    public String inviteMember(int projectId, String projectName, String identifier, String role, String inviterName) {
        int targetUserId = -1;
        String targetEmail = "";

        // 0. Chuẩn hoá giá trị
        String normalized = identifier == null ? "" : identifier.trim();
        if (normalized.startsWith("@")) {
            normalized = normalized.substring(1).trim();
        }
        if (normalized.isEmpty()) {
            return "Vui lòng cung cấp Username hoặc Email!";
        }

        // 1. Tìm người dùng bằng Username HOẶC Email
        String sqlFind = "SELECT ID, Email FROM TBL_USERS WHERE Email = ? OR Username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sqlFind)) {
            pstmt.setString(1, normalized);
            pstmt.setString(2, normalized);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                targetUserId = rs.getInt("ID");
                targetEmail = rs.getString("Email");
            }
        } catch (SQLException e) {
            return "Lỗi hệ thống khi tìm User: " + e.getMessage();
        }

        if (targetUserId == -1)
            return "Không tìm thấy người dùng này!";

        // 2. Insert vào dự án với trạng thái chờ xác nhận (PENDING)
        String sqlInsert = "INSERT INTO TBL_PROJECT_MEMBERS (ProjectID, UserID, Role, InviteStatus) VALUES (?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, targetUserId);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // Nếu bảng chưa có cột InviteStatus, thử fallback sang bản cũ (không lưu
            // InviteStatus)
            if ("42S22".equals(e.getSQLState()) || e.getMessage().contains("Unknown column 'InviteStatus'")) {
                String sqlFallback = "INSERT INTO TBL_PROJECT_MEMBERS (ProjectID, UserID, Role) VALUES (?, ?, ?)";
                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                        PreparedStatement pstmt = conn.prepareStatement(sqlFallback)) {
                    pstmt.setInt(1, projectId);
                    pstmt.setInt(2, targetUserId);
                    pstmt.setString(3, role);
                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    if (ex.getErrorCode() == 1062 || "23000".equals(ex.getSQLState())) {
                        return "Người này đã ở trong dự án hoặc đã được mời rồi!";
                    }
                    return "Lỗi khi mời thành viên: " + ex.getMessage();
                }
            } else if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState())) {
                return "Người này đã ở trong dự án hoặc đã được mời rồi!";
            } else {
                return "Lỗi khi mời thành viên: " + e.getMessage();
            }
        }

        // 3. Tạo thông báo (Quả chuông góc phải)
        System.out.println("DEBUG: Tạo thông báo cho userId=" + targetUserId + ", projectId=" + projectId);
        new NotificationDAO().createInviteNotification(targetUserId, projectId, projectName, inviterName);
        System.out.println("DEBUG: Thông báo đã tạo xong");

        // 4. Bắn Email thực tế
        System.out.println("DEBUG: Gửi email tới " + targetEmail);
        try {
            if (targetEmail != null && !targetEmail.isEmpty()) {
                String msg = "Xin chào,\n\nBạn vừa được " + inviterName + " mời tham gia dự án '" + projectName
                        + "' với vai trò " + role + ".\n\nHãy đăng nhập vào hệ thống để đồng ý tham gia nhé!";
                EmailService.sendEmail(targetEmail, "Lời mời tham gia dự án mới", msg);
            } else {
                System.out.println("DEBUG: Email rỗng, bỏ qua gửi");
            }
        } catch (Exception e) {
            System.err.println("Gửi email thất bại: " + e.getMessage());
        }

        return "SUCCESS";
    }

    // Hàm dùng khi người dùng bấm Đồng ý hoặc Từ chối quả chuông
    public boolean respondToInvite(int userId, int projectId, boolean isAccept) {
        String sql = isAccept
                ? "UPDATE TBL_PROJECT_MEMBERS SET InviteStatus = 'JOINED' WHERE UserID = ? AND ProjectID = ?"
                : "DELETE FROM TBL_PROJECT_MEMBERS WHERE UserID = ? AND ProjectID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, projectId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (isAccept
                    && ("42S22".equals(e.getSQLState()) || e.getMessage().contains("Unknown column 'InviteStatus'"))) {
                // Schema cũ không có cột InviteStatus, vẫn xem như đã tham gia.
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
}