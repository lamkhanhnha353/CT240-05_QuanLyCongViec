package com.teamwork.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.teamwork.db.UserDAO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AuthHandler extends BaseHandler {
    private UserDAO userDAO = new UserDAO();

    // Xử lý Login
    public void handleLogin(HttpExchange exchange) throws IOException {
        handleCors(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                String loginResult = userDAO.login(extract(body, "username"), extract(body, "password"));
                if ("BANNED".equals(loginResult))
                    sendResponse(exchange, 403, "{\"success\": false, \"message\": \"Tài khoản bị khóa!\"}");
                else if (loginResult != null) {
                    String[] parts = loginResult.split("-", 3);
                    sendResponse(exchange, 200, "{\"success\": true, \"userId\": " + parts[0] + ", \"role\": \""
                            + parts[1] + "\", \"fullName\": \"" + escapeJson(parts[2]) + "\"}");
                } else
                    sendResponse(exchange, 401, "{\"success\": false, \"message\": \"Sai tài khoản/mật khẩu\"}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"success\": false}");
            }
        }
    }

    // Xử lý Register
    public void handleRegister(HttpExchange exchange) throws IOException {
        handleCors(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                if (userDAO.register(extract(body, "username"), extract(body, "password"),
                        extract(body, "fullName"), extract(body, "email")))
                    sendResponse(exchange, 200, "{\"success\": true}");
                else
                    sendResponse(exchange, 400,
                            "{\"success\": false, \"message\": \"Tài khoản hoặc email đã tồn tại\"}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
            }
        }
    }

    // Xử lý Quên mật khẩu
    public void handleForgotPassword(HttpExchange exchange) throws IOException {
        handleCors(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String email = extract(body, "email");

                if (email == null || email.isEmpty()) {
                    sendResponse(exchange, 400, "{\"error\": \"Email không hợp lệ!\"}");
                    return;
                }
                String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                StringBuilder newPass = new StringBuilder();
                java.util.Random rnd = new java.util.Random();
                for (int i = 0; i < 6; i++) {
                    newPass.append(chars.charAt(rnd.nextInt(chars.length())));
                }
                String generatedPassword = newPass.toString();

                if (userDAO.resetPassword(email, generatedPassword)) {
                    new Thread(() -> {
                        try {
                            String subject = "[Teamwork Master] Khôi phục mật khẩu";
                            String content = "Xin chào,\n\nBạn đã yêu cầu khôi phục mật khẩu...\nMật khẩu mới: "
                                    + generatedPassword + "\n\nTrân trọng,\nHệ thống Teamwork Master";
                            com.teamwork.plugins.EmailService.sendEmail(email, subject, content);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                    sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Đã gửi mật khẩu mới\"}");
                } else {
                    sendResponse(exchange, 400, "{\"error\": \"Email không tồn tại hoặc tài khoản đã bị khóa!\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
            }
        }
    }
}