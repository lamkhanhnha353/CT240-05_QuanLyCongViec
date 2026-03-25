package com.teamwork.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.teamwork.db.UserDAO;
import com.teamwork.db.AdminDAO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AdminHandler extends BaseHandler {
    private UserDAO userDAO = new UserDAO();

    public void handleGetUsers(HttpExchange exchange) throws IOException {
        handleCors(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if ("GET".equals(exchange.getRequestMethod()))
            sendResponse(exchange, 200, userDAO.getAllMembersJson());
    }

    public void handleCreateUser(HttpExchange exchange) throws IOException {
        handleCors(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            String reqBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int result = userDAO.addUserByAdmin(extract(reqBody, "username"), extract(reqBody, "password"),
                        extract(reqBody, "fullname"), extract(reqBody, "email"));
                if (result == 1)
                    sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Tạo tài khoản thành công!\"}");
                else if (result == -1)
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Tên tài khoản đã tồn tại!\"}");
                else if (result == -2)
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Email này đã được sử dụng!\"}");
                else
                    sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Lỗi CSDL không xác định!\"}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
            }
        }
    }

    public void handleUpdateUser(HttpExchange exchange) throws IOException {
        handleCors(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            String reqBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int result = userDAO.updateUser(Integer.parseInt(extract(reqBody, "id")), extract(reqBody, "fullname"),
                        extract(reqBody, "email"), extract(reqBody, "password"));
                if (result == 1)
                    sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Cập nhật thành công!\"}");
                else if (result == -2)
                    sendResponse(exchange, 400,
                            "{\"success\": false, \"message\": \"Email này đang được người khác sử dụng!\"}");
                else
                    sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Lỗi Database\"}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
            }
        }
    }

    public void handleDeleteUser(HttpExchange exchange) throws IOException {
        handleCors(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            String reqBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                if (userDAO.deleteUser(Integer.parseInt(extract(reqBody, "id"))))
                    sendResponse(exchange, 200, "{\"success\": true}");
                else
                    sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Không thể xóa tài khoản này!\"}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
            }
        }
    }

    public void handleToggleLockUser(HttpExchange exchange) throws IOException {
        handleCors(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            String reqBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                if (userDAO.toggleUserLock(Integer.parseInt(extract(reqBody, "id"))))
                    sendResponse(exchange, 200, "{\"success\": true}");
                else
                    sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Lỗi cập nhật trạng thái!\"}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
            }
        }
    }

    public void handleDashboardSummary(HttpExchange exchange) throws IOException {
        handleCors(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 200, new AdminDAO().getDashboardSummary());
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}