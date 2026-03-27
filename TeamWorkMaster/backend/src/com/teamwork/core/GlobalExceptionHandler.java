package com.teamwork.core;

import javax.swing.JOptionPane;

/**
 * Cơ chế xử lý lỗi tập trung.
 * Đảm bảo lỗi từ Plugin không làm sập toàn bộ hệ thống (Fault Isolation).
 */
public class GlobalExceptionHandler {
    public static void handle(String module, Exception e) {
        String errorMsg = String.format("[LỖI - %s]: %s", module, e.getMessage());

      
        System.err.println(errorMsg);
        e.printStackTrace();


        JOptionPane.showMessageDialog(null, errorMsg, "Cảnh Báo Hệ Thống", JOptionPane.ERROR_MESSAGE);
    }
}