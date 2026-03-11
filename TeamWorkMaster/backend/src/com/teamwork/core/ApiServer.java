package com.teamwork.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.teamwork.db.UserDAO;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class ApiServer {
    private HttpServer server;
    private UserDAO userDAO;

    public ApiServer(int port) throws IOException {
        userDAO = new UserDAO();
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/admin/users/create", new AdminCreateUserHandler());
        server.createContext("/api/admin/users", new AdminGetUsersHandler());
        server.createContext("/api/admin/users/update", new AdminUpdateUserHandler());

        server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println("[API SERVER] Da khoi dong thanh cong tren cong " + server.getAddress().getPort());
        System.out.println("[API SERVER] Dang lang nghe yeu cau tu Web...");
    }

    class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    String username = requestBody.split("\"username\"\\s*:\\s*\"")[1].split("\"")[0];
                    String password = requestBody.split("\"password\"\\s*:\\s*\"")[1].split("\"")[0];

                    String role = userDAO.login(username, password);

                    if ("BANNED".equals(role)) {
                        sendResponse(exchange, 403,
                                "{\"success\": false, \"message\": \"Tài khoản của bạn đã bị khóa!\"}");
                    } else if (role != null) {
                        sendResponse(exchange, 200, "{\"success\": true, \"role\": \"" + role
                                + "\", \"message\": \"Đăng nhập thành công\"}");
                    } else {
                        sendResponse(exchange, 401,
                                "{\"success\": false, \"message\": \"Sai tài khoản hoặc mật khẩu!\"}");
                    }
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
                }
            }
        }
    }

    class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    String username = requestBody.split("\"username\"\\s*:\\s*\"")[1].split("\"")[0];
                    String password = requestBody.split("\"password\"\\s*:\\s*\"")[1].split("\"")[0];
                    String fullName = requestBody.split("\"fullName\"\\s*:\\s*\"")[1].split("\"")[0];
                    String email = requestBody.split("\"email\"\\s*:\\s*\"")[1].split("\"")[0];

                    boolean isSuccess = userDAO.register(username, password, fullName, email);
                    if (isSuccess) {
                        sendResponse(exchange, 200, "{\"success\": true}");
                    } else {
                        sendResponse(exchange, 400,
                                "{\"success\": false, \"message\": \"Tài khoản hoặc email đã tồn tại\"}");
                    }
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
                }
            }
        }
    }

    class AdminGetUsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 200, userDAO.getAllMembersJson());
            }
        }
    }

    // ==========================================
    // API TẠO TÀI KHOẢN (ĐÃ FIX LỖI CORS)
    // ==========================================
    class AdminCreateUserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 3 DÒNG NÀY RẤT QUAN TRỌNG ĐỂ KHÔNG BỊ "LỖI KẾT NỐI SERVER JAVA"
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String reqBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    String username = reqBody.split("\"username\"\\s*:\\s*\"")[1].split("\"")[0];
                    String fullname = reqBody.split("\"fullname\"\\s*:\\s*\"")[1].split("\"")[0];
                    String email = reqBody.split("\"email\"\\s*:\\s*\"")[1].split("\"")[0];
                    String password = reqBody.split("\"password\"\\s*:\\s*\"")[1].split("\"")[0];

                    // Hàm này gọi qua UserDAO.java của bạn (đã code kiểm tra trùng)
                    int result = userDAO.addUserByAdmin(username, password, fullname, email);

                    if (result == 1) {
                        sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Tạo tài khoản thành công!\"}");
                    } else if (result == -1) {
                        // Báo lỗi trùng Username
                        sendResponse(exchange, 400,
                                "{\"success\": false, \"field\": \"username\", \"message\": \"Tên tài khoản (Username) đã tồn tại trong hệ thống!\"}");
                    } else if (result == -2) {
                        // Báo lỗi trùng Email
                        sendResponse(exchange, 400,
                                "{\"success\": false, \"field\": \"email\", \"message\": \"Email này đã được đăng ký cho một tài khoản khác!\"}");
                    } else {
                        sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Lỗi CSDL không xác định!\"}");
                    }
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
                }
            }
        }
    }

    // ==========================================
    // API CẬP NHẬT TÀI KHOẢN (ĐÃ FIX LỖI CORS)
    // ==========================================
    // ==========================================
    // API CẬP NHẬT TÀI KHOẢN (ĐÃ BỎ PASS CŨ)
    // ==========================================
    class AdminUpdateUserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String reqBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    String idStr = reqBody.split("\"id\"\\s*:\\s*")[1].split("[,}]")[0].trim();
                    int id = Integer.parseInt(idStr);
                    String fullname = reqBody.split("\"fullname\"\\s*:\\s*\"")[1].split("\"")[0];
                    String email = reqBody.split("\"email\"\\s*:\\s*\"")[1].split("\"")[0];

                    String password = "";

                    // Chỉ lấy mật khẩu mới nếu có
                    if (reqBody.contains("\"password\"")) {
                        String[] split = reqBody.split("\"password\"\\s*:\\s*\"");
                        if (split.length > 1 && split[1].startsWith("\""))
                            password = split[1].split("\"")[1];
                    }

                    // Gọi hàm update không có oldPassword
                    int result = userDAO.updateUser(id, fullname, email, password);

                    if (result == 1) {
                        sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Cập nhật thành công!\"}");
                    } else if (result == -2) {
                        sendResponse(exchange, 400,
                                "{\"success\": false, \"field\": \"email\", \"message\": \"Email này đang được một người khác sử dụng!\"}");
                    } else {
                        sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Lỗi Database\"}");
                    }
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
                }
            }
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}