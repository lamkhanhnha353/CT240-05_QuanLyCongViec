package com.teamwork.db;

import java.sql.*;

public class MeetingDAO {

    // 1. Mở phòng họp mới (Trả về ID của cuộc họp vừa tạo)
    public int startMeeting(int projectId, int hostId, String meetLink) {
        String sql = "INSERT INTO TBL_MEETINGS (ProjectID, HostID, MeetLink, Status) VALUES (?, ?, ?, 'ONGOING')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, projectId);
            pstmt.setInt(2, hostId);
            pstmt.setString(3, meetLink);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // 2. Kết thúc cuộc họp và lưu Biên bản
    public boolean endMeeting(int meetingId, String notes) {
        String sql = "UPDATE TBL_MEETINGS SET Status = 'ENDED', EndTime = CURRENT_TIMESTAMP, Notes = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, notes);
            pstmt.setInt(2, meetingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Lấy cuộc họp ĐANG DIỄN RA của dự án (Nếu có)
    public String getCurrentMeeting(int projectId) {
        String sql = "SELECT m.*, u.FullName FROM TBL_MEETINGS m JOIN TBL_USERS u ON m.HostID = u.ID WHERE m.ProjectID = ? AND m.Status = 'ONGOING' LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, projectId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "{"
                        + "\"id\":" + rs.getInt("ID") + ","
                        + "\"hostName\":\"" + escapeJson(rs.getString("FullName")) + "\","
                        + "\"meetLink\":\"" + escapeJson(rs.getString("MeetLink")) + "\""
                        + "}";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "null"; // Trả về null string nếu không có họp
    }

    // 4. Lấy LỊCH SỬ các cuộc họp ĐÃ KẾT THÚC
    public String getMeetingHistory(int projectId) {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT m.*, u.FullName FROM TBL_MEETINGS m JOIN TBL_USERS u ON m.HostID = u.ID WHERE m.ProjectID = ? AND m.Status = 'ENDED' ORDER BY m.EndTime DESC";
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
                        .append("\"hostName\":\"").append(escapeJson(rs.getString("FullName"))).append("\",")
                        .append("\"startTime\":\"").append(rs.getTimestamp("StartTime")).append("\",")
                        .append("\"endTime\":\"").append(rs.getTimestamp("EndTime")).append("\",")
                        .append("\"notes\":\"").append(escapeJson(rs.getString("Notes"))).append("\"")
                        .append("}");
                first = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json.append("]").toString();
    }

    // Tiện ích: Lấy danh sách ID và Email của thành viên để gửi thông báo
    public ResultSet getProjectMembersForNotification(int projectId) throws SQLException {
        String sql = "SELECT u.ID, u.Email FROM TBL_PROJECT_MEMBERS pm JOIN TBL_USERS u ON pm.UserID = u.ID WHERE pm.ProjectID = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, projectId);
        return pstmt.executeQuery();
    }

    // 5. Cập nhật lại Biên bản cuộc họp (Chỉ cập nhật Notes)
    public boolean updateMeetingNotes(int meetingId, String newNotes) {
        String sql = "UPDATE TBL_MEETINGS SET Notes = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newNotes);
            pstmt.setInt(2, meetingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String escapeJson(String data) {
        if (data == null)
            return "";
        return data.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}