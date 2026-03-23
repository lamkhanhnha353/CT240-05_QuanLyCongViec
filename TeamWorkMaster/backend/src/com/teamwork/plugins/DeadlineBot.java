package com.teamwork.plugins; // Thay đổi package cho đúng với project của bạn

import com.teamwork.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeadlineBot {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void startBot() {
        System.out.println("🤖 [Deadline Bot] Đã khởi động! Đang tính toán thời gian đến 8h sáng...");

        Runnable scanTask = () -> scanAndRemind();

        // 1. Lấy thời gian hiện tại theo múi giờ Việt Nam
        java.time.LocalDateTime now = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));

        // 2. Đặt mục tiêu là 8 giờ 00 phút 00 giây sáng nay
        java.time.LocalDateTime nextRun = now.withHour(8).withMinute(0).withSecond(0);

        // 3. Nếu bây giờ đã qua 8h sáng, thì dời mục tiêu sang 8h sáng ngày mai
        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }

        // 4. Tính xem từ bây giờ đến mục tiêu còn bao nhiêu PHÚT
        long initialDelay = java.time.temporal.ChronoUnit.MINUTES.between(now, nextRun);

        // 5. Ra lệnh cho Scheduler: Chờ initialDelay phút rồi chạy, sau đó cứ 1440 phút
        // (24h) chạy 1 lần
        scheduler.scheduleAtFixedRate(scanTask, initialDelay, java.util.concurrent.TimeUnit.DAYS.toMinutes(1),
                TimeUnit.MINUTES);

        System.out.println("⏰ [Deadline Bot] Lịch trình đã được set! Sẽ gửi email vào đúng 08:00 AM mỗi ngày.");
        System.out.println("⏳ (Lần quét đầu tiên sẽ diễn ra sau: " + (initialDelay / 60) + " giờ " + (initialDelay % 60)
                + " phút nữa)");
    }

    private static void scanAndRemind() {
        System.out.println("🔍 [Deadline Bot] Đang quét toàn bộ công việc trong hệ thống...");

        // 🟢 ĐÃ FIX LỖI: Đổi p.Name thành p.ProjectName cho khớp với bảng TBL_PROJECTS
        // 🟢
        String taskSql = "SELECT t.ID, t.Title, t.Deadline, t.Status, p.ProjectName AS ProjectName, " +
                "COALESCE((SELECT GROUP_CONCAT(UserID) FROM TBL_TASK_ASSIGNEES WHERE TaskID = t.ID), t.AssigneeID) AS AssigneeIds "
                +
                "FROM TBL_TASKS t " +
                "JOIN TBL_PROJECTS p ON t.ProjectID = p.ID " +
                "WHERE t.Status NOT IN ('DONE', 'CANCEL') AND t.Deadline IS NOT NULL";

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(taskSql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String taskTitle = rs.getString("Title");
                String deadlineStr = rs.getString("Deadline");
                String assigneeIds = rs.getString("AssigneeIds");
                String projectName = rs.getString("ProjectName");

                // Nếu thẻ này không có ai làm thì bỏ qua không cần thông báo
                if (assigneeIds == null || assigneeIds.trim().isEmpty()) {
                    continue;
                }

                // Parse ngày tháng của MySQL (YYYY-MM-DD)
                LocalDate deadlineDate = LocalDate.parse(deadlineStr);
                long daysLeft = ChronoUnit.DAYS.between(today, deadlineDate);

                String emailSubject = "";
                String emailBody = "";

                // Phân tích rủi ro thời gian
                if (daysLeft < 0) {
                    emailSubject = "[KHẨN CẤP] Trễ hạn công việc - Dự án: " + projectName;
                    emailBody = "Chào bạn,\n\n" +
                            "Hệ thống phát hiện bạn có một công việc ĐÃ TRỄ HẠN " + Math.abs(daysLeft) + " ngày.\n" +
                            "📌 Dự án: " + projectName + "\n" +
                            "📝 Công việc: " + taskTitle + "\n" +
                            "📅 Hạn chót: " + deadlineDate.format(formatter) + "\n\n" +
                            "Vui lòng truy cập hệ thống và cập nhật tiến độ ngay lập tức để không ảnh hưởng đến team!\n\n"
                            +
                            "Trân trọng,\nTeamwork Master System.";

                } else if (daysLeft == 0) {
                    emailSubject = "[NHẮC NHỞ] Công việc tới hạn hôm nay - Dự án: " + projectName;
                    emailBody = "Chào bạn,\n\n" +
                            "Hôm nay là hạn chót cho công việc của bạn. Hãy cố gắng hoàn thành nhé!\n" +
                            "📌 Dự án: " + projectName + "\n" +
                            "📝 Công việc: " + taskTitle + "\n\n" +
                            "Trân trọng,\nTeamwork Master System.";

                } else if (daysLeft == 3) {
                    emailSubject = "[THÔNG BÁO] Sắp tới hạn công việc - Dự án: " + projectName;
                    emailBody = "Chào bạn,\n\n" +
                            "Bạn có một công việc sắp tới hạn trong 3 ngày tới.\n" +
                            "📌 Dự án: " + projectName + "\n" +
                            "📝 Công việc: " + taskTitle + "\n" +
                            "📅 Hạn chót: " + deadlineDate.format(formatter) + "\n\n" +
                            "Trân trọng,\nTeamwork Master System.";
                }

                // Gửi Email cho danh sách người làm
                if (!emailSubject.isEmpty()) {
                    String[] userIds = assigneeIds.split(",");
                    for (String uid : userIds) {
                        uid = uid.trim();
                        if (!uid.isEmpty()) {
                            sendEmailToUser(conn, uid, emailSubject, emailBody);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ [Deadline Bot] Lỗi khi quét Database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void sendEmailToUser(Connection conn, String userId, String subject, String message) {
        String sql = "SELECT Email, FullName FROM TBL_USERS WHERE ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(userId));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String email = rs.getString("Email");
                    String fullName = rs.getString("FullName");

                    // Cá nhân hóa nội dung Email
                    String personalizedMessage = message.replace("Chào bạn", "Chào " + fullName);

                    // Gửi Email
                    EmailService.sendEmail(email, subject, personalizedMessage);
                    System.out.println("➡️ [Deadline Bot] Đã gửi cảnh báo tới: " + email);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}