package com.teamwork.patterns;

import com.teamwork.plugins.EmailService;

public class EmailNotificationStrategy implements NotificationStrategy {
    @Override
    public void send(String recipient, String title, String message) {
        try {
            EmailService.sendEmail(recipient, title, message);
            System.out.println("[STRATEGY] Đã gửi thông báo qua Email tới: " + recipient);
        } catch (Exception e) {
            System.out.println("Lỗi gửi Email: " + e.getMessage());
        }
    }
}