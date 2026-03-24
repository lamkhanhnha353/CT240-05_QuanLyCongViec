package com.teamwork.plugins;

import com.teamwork.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TaskActionNotifier {

    // 💥 1. CÓ VIỆC MỚI TỚI TAY (🟢 Đã thêm tham số int projectId)
    public static void notifyNewTask(int projectId, String projectName, String taskTitle, String assigneeIds,
            String assignerName) {
        if (assigneeIds == null || assigneeIds.trim().isEmpty() || assigneeIds.equals("null"))
            return;

        String subject = "[NHIỆM VỤ MỚI] Bạn vừa được giao việc trong dự án: " + projectName;
        String message = "Chào bạn,\n\n"
                + assignerName + " vừa giao cho bạn một công việc mới.\n"
                + "📌 Dự án: " + projectName + "\n"
                + "📝 Công việc: " + taskTitle + "\n\n"
                + "Vui lòng truy cập hệ thống và bắt đầu thực hiện nhé.\n\n"
                + "Trân trọng,\nTeamwork Master System.";

        sendToAssignees(assigneeIds, subject, message, projectId);
    }

    // 💥 2. BÁO CÁO HOÀN THÀNH
    public static void notifyTaskCompleted(int projectId, String taskTitle, int completerId, String completerName) {
        String sql = "SELECT u.ID as OwnerID, u.Email, u.FullName, p.ProjectName FROM TBL_PROJECTS p "
                + "JOIN TBL_USERS u ON p.OwnerID = u.ID WHERE p.ID = ?";

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, projectId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int ownerId = rs.getInt("OwnerID");

                        if (ownerId == completerId) {
                            System.out.println(
                                    "➡️ [Email-Thread] Người làm xong chính là Sếp. Đã tự biết, bỏ qua việc báo cáo!");
                            return;
                        }

                        String ownerName = rs.getString("FullName");
                        String projectName = rs.getString("ProjectName");

                        String subject = "[HOÀN THÀNH] Công việc đã xong - Dự án: " + projectName;
                        String message = "Chào " + ownerName + ",\n\n"
                                + "Thành viên " + completerName + " vừa đánh dấu HOÀN THÀNH công việc sau:\n"
                                + "📌 Dự án: " + projectName + "\n"
                                + "✅ Công việc: " + taskTitle + "\n\n"
                                + "Bạn có thể vào hệ thống để kiểm tra nghiệm thu.\n\n"
                                + "Trân trọng,\nTeamwork Master System.";

                        // 🟢 Gọi qua hàm sendToAssignees để vừa Lưu Chuông Web, vừa Gửi Mail cho Sếp
                        sendToAssignees(String.valueOf(ownerId), subject, message, projectId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 💥 3. LÉN ĐỔI DEADLINE (🟢 Đã thêm tham số int projectId)
    public static void notifyDeadlineChanged(int projectId, String projectName, String taskTitle, String assigneeIds,
            String oldDeadline, String newDeadline, String changerName) {
        if (assigneeIds == null || assigneeIds.trim().isEmpty() || assigneeIds.equals("null"))
            return;

        String subject = "[THAY ĐỔI DEADLINE] Công việc của bạn vừa bị chỉnh sửa hạn chót!";
        String message = "Chào bạn,\n\n"
                + changerName + " vừa thay đổi hạn chót (Deadline) công việc của bạn.\n"
                + "📌 Dự án: " + projectName + "\n"
                + "📝 Công việc: " + taskTitle + "\n"
                + "⏰ Hạn cũ: " + (oldDeadline != null && !oldDeadline.equals("null") ? oldDeadline : "Chưa có hạn")
                + "\n"
                + "🚨 HẠN MỚI: " + newDeadline + "\n\n"
                + "Vui lòng chú ý thời gian để hoàn thành đúng tiến độ nhé!\n\n"
                + "Trân trọng,\nTeamwork Master System.";

        sendToAssignees(assigneeIds, subject, message, projectId);
    }

    // ==========================================
    // 🟢 HÀM XỬ LÝ LƯU DATABASE VÀ GỬI MAIL (ĐÃ NÂNG CẤP)
    // ==========================================
    private static void sendToAssignees(String assigneeIds, String subject, String message, int projectId) {
        String sql = "SELECT Email, FullName FROM TBL_USERS WHERE ID = ?";
        String[] ids = assigneeIds.split(",");

        com.teamwork.db.NotificationDAO notifDAO = new com.teamwork.db.NotificationDAO();

        for (String idStr : ids) {
            if (idStr.trim().isEmpty())
                continue;

            int userId = Integer.parseInt(idStr.trim());

            // 1. Lưu vào Database ngay lập tức để hiện Quả Chuông trên Web
            try {
                notifDAO.addNotification(userId, projectId, subject, message);
            } catch (Exception e) {
                System.err.println("❌ Lỗi lưu thông báo vào DB cho User ID: " + userId);
            }

            // 2. Mở luồng lấy thông tin và Gửi Email ngầm
            try {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, userId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            String email = rs.getString("Email");
                            String fullName = rs.getString("FullName");

                            // Cá nhân hóa nội dung Email
                            String personalizedMessage = message.replace("Chào bạn", "Chào " + fullName);

                            // Chạy ngầm Gửi Mail để không làm chậm API
                            new Thread(() -> {
                                try {
                                    EmailService.sendEmail(email, subject, personalizedMessage);
                                } catch (Exception ignored) {
                                }
                            }).start();
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi lấy thông tin gửi mail cho User ID: " + userId);
                e.printStackTrace();
            }
        }
    }
}