package com.teamwork.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskDAO {

    /**
     * Hàm đếm số lượng công việc theo trạng thái
     * Trả về mảng 3 số nguyên tương ứng: [Số lượng To Do, Số lượng In Progress, Số
     * lượng Done]
     */
    public int[] getTaskStatistics() {
        int[] stats = new int[3]; // Khởi tạo mảng có 3 phần tử mặc định là 0

        // Mình giả định bảng tên là 'tasks' và cột trạng thái là 'status'.
        // (Nếu database của nhóm dùng 'tbl_tasks' hay cột tên khác thì bạn chỉ cần sửa
        // lại câu SQL này nhé)
        String sql = "SELECT status, COUNT(*) as total FROM tasks GROUP BY status";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        if (conn == null) {
            System.err.println("[TaskDAO] Lỗi: Không thể lấy kết nối Database.");
            return stats;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("total");

                if (status != null) {
                    // Chuyển về chữ thường để dễ so sánh, phòng trường hợp viết hoa viết thường
                    // khác nhau
                    String s = status.toLowerCase();

                    // Phân loại thông minh vào 3 nhóm
                    if (s.contains("to do") || s.contains("todo") || s.equals("0")) {
                        stats[0] += count; // Cộng vào nhóm To Do
                    } else if (s.contains("in progress") || s.contains("progress") || s.equals("1")) {
                        stats[1] += count; // Cộng vào nhóm In Progress
                    } else if (s.contains("done") || s.equals("2")) {
                        stats[2] += count; // Cộng vào nhóm Done
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[TaskDAO] Lỗi khi thống kê công việc: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }
}