package com.teamwork.core;

import com.sun.net.httpserver.*;
import com.teamwork.db.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

public class ApiServer {
    private HttpServer server;
    private UserDAO userDAO;

    public ApiServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        userDAO = new UserDAO();

        server.createContext("/api/projects/list", this::handleList);
        server.createContext("/api/projects/create", this::handleCreate);
        server.createContext("/api/projects/update", this::handleUpdate);
        server.createContext("/api/projects/delete", this::handleDelete);
        server.createContext("/api/projects/add-member", this::handleAddMember);

        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/admin/users/create", new AdminCreateUserHandler());
        server.createContext("/api/admin/users", new AdminGetUsersHandler());
        server.createContext("/api/admin/users/update", new AdminUpdateUserHandler());
        server.createContext("/api/admin/users/delete", new AdminDeleteUserHandler());
        server.createContext("/api/admin/users/toggle-lock", new AdminToggleLockUserHandler());

        server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println(">>> BACKEND ĐANG CHẠY TẠI CỔNG 8080");
        System.out.println(">>> TRẠNG THÁI: SẴN SÀNG KẾT NỐI VỚI VUE");
    }

    private void handleList(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            sendResponse(ex, 200, new ProjectDAO().getAllProjectsJson());
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleCreate(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            String name = extract(body, "name");
            String desc = extract(body, "desc");
            String start = extract(body, "startDate");
            String end = extract(body, "endDate");

            // Tạm fix cứng ID người dùng đang đăng nhập là 1
            int userId = 1;

            int pid = new ProjectDAO().createProject(name, desc, start, end, userId);

            if (pid != -1) {
                // 1. Tự động tạo 3 cột trạng thái cho dự án
                new StatusDAO().createDefaultStatus(pid);

                // 2. Set quyền OWNER cho người tạo dự án
                new ProjectMemberDAO().addOrUpdateMember(pid, userId, "OWNER");

                // 3. Ghi log hoạt động
                new ActivityLogDAO().log(pid, userId, "Đã khởi tạo dự án: " + name);

                sendResponse(ex, 201, "{\"message\":\"Tạo thành công\", \"id\":" + pid + "}");
            } else {
                sendResponse(ex, 400, "{\"error\":\"Tạo thất bại. Kiểm tra CSDL!\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        }
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
                    if (isSuccess)
                        sendResponse(exchange, 200, "{\"success\": true}");
                    else
                        sendResponse(exchange, 400,
                                "{\"success\": false, \"message\": \"Tài khoản hoặc email đã tồn tại\"}");
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
            if ("GET".equals(exchange.getRequestMethod()))
                sendResponse(exchange, 200, userDAO.getAllMembersJson());
        }
    }

    class AdminCreateUserHandler implements HttpHandler {
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
                    String username = reqBody.split("\"username\"\\s*:\\s*\"")[1].split("\"")[0];
                    String fullname = reqBody.split("\"fullname\"\\s*:\\s*\"")[1].split("\"")[0];
                    String email = reqBody.split("\"email\"\\s*:\\s*\"")[1].split("\"")[0];
                    String password = reqBody.split("\"password\"\\s*:\\s*\"")[1].split("\"")[0];

                    int result = userDAO.addUserByAdmin(username, password, fullname, email);

                    if (result == 1)
                        sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Tạo tài khoản thành công!\"}");
                    else if (result == -1)
                        sendResponse(exchange, 400,
                                "{\"success\": false, \"field\": \"username\", \"message\": \"Tên tài khoản đã tồn tại!\"}");
                    else if (result == -2)
                        sendResponse(exchange, 400,
                                "{\"success\": false, \"field\": \"email\", \"message\": \"Email này đã được sử dụng!\"}");
                    else
                        sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Lỗi CSDL không xác định!\"}");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
                }
            }
        }
    }

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

                    if (reqBody.contains("\"password\"")) {
                        String[] split = reqBody.split("\"password\"\\s*:\\s*\"");
                        if (split.length > 1 && split[1].startsWith("\""))
                            password = split[1].split("\"")[1];
                    }

                    int result = userDAO.updateUser(id, fullname, email, password);

                    if (result == 1)
                        sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Cập nhật thành công!\"}");
                    else if (result == -2)
                        sendResponse(exchange, 400,
                                "{\"success\": false, \"field\": \"email\", \"message\": \"Email này đang được người khác sử dụng!\"}");
                    else
                        sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Lỗi Database\"}");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
                }
            }
        }
    }

    class AdminDeleteUserHandler implements HttpHandler {
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
                    boolean isDeleted = userDAO.deleteUser(id);
                    if (isDeleted)
                        sendResponse(exchange, 200, "{\"success\": true}");
                    else
                        sendResponse(exchange, 500,
                                "{\"success\": false, \"message\": \"Không thể xóa tài khoản này!\"}");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
                }
            }
        }
    }

    class AdminToggleLockUserHandler implements HttpHandler {
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
                    boolean isSuccess = userDAO.toggleUserLock(id);
                    if (isSuccess)
                        sendResponse(exchange, 200, "{\"success\": true}");
                    else
                        sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Lỗi cập nhật trạng thái!\"}");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
                }
            }
        }
    }

    private void handleUpdate(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int pid = Integer.parseInt(extract(body, "projectId"));
            boolean ok = new ProjectDAO().updateProject(pid, extract(body, "name"), extract(body, "desc"),
                    extract(body, "status"), extract(body, "endDate"));
            sendResponse(ex, ok ? 200 : 400, ok ? "{\"msg\":\"Xong\"}" : "{\"error\":\"Lỗi\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleDelete(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int pid = Integer.parseInt(extract(body, "projectId"));
            boolean ok = new ProjectDAO().deleteProject(pid);
            sendResponse(ex, ok ? 200 : 400, ok ? "{\"msg\":\"Xóa xong\"}" : "{\"error\":\"Lỗi\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleAddMember(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int projectId = Integer.parseInt(extract(body, "projectId"));
            // ĐÃ SỬA: Lấy email thay vì userId
            String email = extract(body, "email");
            String role = extract(body, "role");

            // Gọi hàm addMemberByEmail mới tạo
            boolean ok = new ProjectMemberDAO().addMemberByEmail(projectId, email, role);

            if (ok) {
                sendResponse(ex, 200, "{\"message\":\"Thêm thành viên thành công!\"}");
            } else {
                sendResponse(ex, 400, "{\"error\":\"Thêm thất bại. Email này chưa đăng ký tài khoản!\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleCors(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
    }

    private String extract(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\":\\s*\"?(.*?)\"?(?:[,}]|\\s)").matcher(json);
        return m.find() ? m.group(1).trim() : "";
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