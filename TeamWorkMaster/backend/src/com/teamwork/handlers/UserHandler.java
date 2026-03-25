package com.teamwork.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.teamwork.db.UserDAO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UserHandler extends BaseHandler {
    private UserDAO userDAO = new UserDAO();

    // Tìm kiếm User để mời vào dự án
    public void handleSearchUsers(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String keyword = extractQuery(ex.getRequestURI().getQuery(), "q");
            if (!keyword.isEmpty())
                keyword = java.net.URLDecoder.decode(keyword, StandardCharsets.UTF_8.name());
            sendResponse(ex, 200, userDAO.searchUsers(keyword));
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // Lấy thông tin Profile cá nhân
    public void handleUserProfile(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            int userId = Integer.parseInt(extractQuery(ex.getRequestURI().getQuery(), "id"));
            String profile = userDAO.getUserProfile(userId);
            if (profile != null)
                sendResponse(ex, 200, profile);
            else
                sendResponse(ex, 404, "{\"error\": \"User not found\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // Đổi mật khẩu
    public void handleChangePassword(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int userId = Integer.parseInt(extract(body, "userId"));
            String oldPass = extract(body, "oldPassword");
            String newPass = extract(body, "newPassword");

            if (userDAO.changePassword(userId, oldPass, newPass))
                sendResponse(ex, 200, "{\"success\": true}");
            else
                sendResponse(ex, 400, "{\"success\": false, \"message\": \"Mật khẩu cũ không chính xác!\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}