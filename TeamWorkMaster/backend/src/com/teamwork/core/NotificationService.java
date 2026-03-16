package com.teamwork.core;

import com.teamwork.plugins.EmailService;

public class NotificationService {

        private static void sendNotification(Task task, String subject, String message, String icon) {

                System.out.println(icon + " " + message);

                EmailService.sendEmail(
                                task.getUserEmail(),
                                subject,
                                message);
        }

        public static void notifyDeadlineSoon(Task task) {

                String msg = "Công việc '" + task.getName()
                                + "' còn 3 ngày nữa đến deadline.";

                sendNotification(task, "Nhắc nhở deadline", msg, "⏰");
        }

        public static void notifyDeadlineToday(Task task) {

                String msg = "Hôm nay là deadline của công việc '"
                                + task.getName() + "'.";

                sendNotification(task, "Deadline hôm nay", msg, "📅");
        }

        public static void notifyOverdue(Task task) {

                String msg = "Công việc '" + task.getName()
                                + "' đã quá deadline.";

                sendNotification(task, "Công việc quá hạn", msg, "⚠️");
        }

        public static void notifyStatusChange(Task task) {

                String msg = "Trạng thái công việc '" + task.getName()
                                + "' đã đổi thành: " + task.getStatus();

                sendNotification(task, "Cập nhật trạng thái công việc", msg, "🔔");
        }
}