package com.teamwork.core;

import javax.swing.JOptionPane;

/**
 * Cơ chế xử lý lỗi tập trung.
 * Đảm bảo lỗi từ Plugin không làm sập toàn bộ hệ thống (Fault Isolation).
 */
public class GlobalExceptionHandler {
    public static void handle(String module, Exception e) {
        String errorMsg = String.format("[LỖI - %s]: %s", module, e.getMessage());

        // Ghi log ra console hệ thống
        System.err.println(errorMsg);
        e.printStackTrace();

        // Hiển thị hộp thoại cảnh báo cho Admin
        JOptionPane.showMessageDialog(null, errorMsg, "Cảnh Báo Hệ Thống", JOptionPane.ERROR_MESSAGE);
    }
}