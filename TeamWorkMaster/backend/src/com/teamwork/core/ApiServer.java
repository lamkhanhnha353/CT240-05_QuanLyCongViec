package com.teamwork.core;

import com.sun.net.httpserver.*;
import com.teamwork.db.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;
import com.teamwork.service.PermissionService;

public class ApiServer {
    private HttpServer server;
    private UserDAO userDAO;

    public ApiServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        userDAO = new UserDAO();

        // --- CÁC ENDPOINT DỰ ÁN ---
        server.createContext("/api/projects/list", this::handleList);
        server.createContext("/api/projects/create", this::handleCreate);
        server.createContext("/api/projects/update", this::handleUpdate);
        server.createContext("/api/projects/delete", this::handleDelete);
        server.createContext("/api/projects/add-member", this::handleAddMember);

        // --- CÁC ENDPOINT TASK (CÔNG VIỆC) ---
        server.createContext("/api/tasks/list", this::handleTaskList);
        server.createContext("/api/tasks/create", this::handleTaskCreate);
        server.createContext("/api/tasks/update-status", this::handleTaskUpdateStatus);

        // --- CÁC ENDPOINT AUTH & ADMIN ---
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/admin/users/create", new AdminCreateUserHandler());
        server.createContext("/api/admin/users", new AdminGetUsersHandler());
        server.createContext("/api/admin/users/update", new AdminUpdateUserHandler());
        server.createContext("/api/admin/users/delete", new AdminDeleteUserHandler());
        server.createContext("/api/admin/users/toggle-lock", new AdminToggleLockUserHandler());

        // --- CÁC ENDPOINT THÔNG BÁO & TÌM KIẾM ---
        server.createContext("/api/users/search", this::handleSearchUsers);
        server.createContext("/api/notifications/list", this::handleGetNotifications);
        server.createContext("/api/notifications/respond", this::handleRespondInvite);

        server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println(">>> BACKEND ĐANG CHẠY TẠI CỔNG " + server.getAddress().getPort());
        System.out.println(">>> TRẠNG THÁI: SẴN SÀNG KẾT NỐI VỚI VUE");
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ TASK (KANBAN BOARD)
    // ==========================================
    private void handleTaskList(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String query = ex.getRequestURI().getQuery();
            int projectId = 0;
            if (query != null && query.contains("projectId=")) {
                projectId = Integer.parseInt(query.split("projectId=")[1].split("&")[0]);
            }
            sendResponse(ex, 200, new TaskDAO().getTasksByProject(projectId));
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleTaskCreate(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int projectId = Integer.parseInt(extract(body, "projectId"));
            String title = extract(body, "title");
            String desc = extract(body, "description");
            if (desc.isEmpty())
                desc = extract(body, "desc");
            String priority = extract(body, "priority");
            if (priority.isEmpty())
                priority = "MEDIUM";
            String deadline = extract(body, "deadline");

            String assigneeStr = extract(body, "assigneeId");
            int assigneeId = assigneeStr.isEmpty() ? 0 : Integer.parseInt(assigneeStr);

            boolean ok = new TaskDAO().createTask(projectId, title, desc, priority, deadline, assigneeId);
            if (ok)
                sendResponse(ex, 201, "{\"message\":\"Tạo công việc thành công\"}");
            else
                sendResponse(ex, 400, "{\"error\":\"Tạo thất bại do lỗi CSDL\"}");
        } catch (Exception e) {
            sendResponse(ex, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleTaskUpdateStatus(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int taskId = Integer.parseInt(extract(body, "taskId"));
            String status = extract(body, "status");

            boolean ok = new TaskDAO().updateTaskStatus(taskId, status);
            if (ok)
                sendResponse(ex, 200, "{\"message\":\"Cập nhật vị trí thành công\"}");
            else
                sendResponse(ex, 400, "{\"error\":\"Lỗi CSDL khi cập nhật\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ DỰ ÁN & PROJECT (Giữ nguyên)
    // ==========================================
    private void handleList(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int currentUserId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;
            sendResponse(ex, 200, new ProjectDAO().getAllProjectsJson(currentUserId));
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
            if (desc.isEmpty())
                desc = extract(body, "description");
            String start = extract(body, "startDate");
            String end = extract(body, "endDate");
            String priority = extract(body, "priority");
            if (priority.isEmpty())
                priority = "MEDIUM";

            int userId = Integer.parseInt(extract(body, "userId"));

            int pid = new ProjectDAO().createProject(name, desc, start, end, priority, userId);
            if (pid != -1) {
                new StatusDAO().createDefaultStatus(pid);
                new ProjectMemberDAO().addOrUpdateMember(pid, userId, "OWNER");
                new ActivityLogDAO().log(pid, userId, "Đã khởi tạo dự án: " + name);
                sendResponse(ex, 201, "{\"message\":\"Tạo thành công\"}");
            } else {
                sendResponse(ex, 400, "{\"error\":\"Tạo thất bại\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    String username = extract(body, "username");
                    String password = extract(body, "password");
                    String loginResult = userDAO.login(username, password);

                    if ("BANNED".equals(loginResult)) {
                        sendResponse(exchange, 403, "{\"success\": false, \"message\": \"Tài khoản bị khóa!\"}");
                    } else if (loginResult != null) {
                        String[] parts = loginResult.split("-");
                        sendResponse(exchange, 200,
                                "{\"success\": true, \"userId\": " + parts[0] + ", \"role\": \"" + parts[1] + "\"}");
                    } else {
                        sendResponse(exchange, 401, "{\"success\": false, \"message\": \"Sai tài khoản/mật khẩu\"}");
                    }
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\": false}");
                }
            }
        }
    }

    class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    String username = extract(requestBody, "username");
                    String password = extract(requestBody, "password");
                    String fullName = extract(requestBody, "fullName");
                    String email = extract(requestBody, "email");

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
            handleCors(exchange);
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
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                String reqBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    String username = extract(reqBody, "username");
                    String fullname = extract(reqBody, "fullname");
                    String email = extract(reqBody, "email");
                    String password = extract(reqBody, "password");

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
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                String reqBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    int id = Integer.parseInt(extract(reqBody, "id"));
                    String fullname = extract(reqBody, "fullname");
                    String email = extract(reqBody, "email");
                    String password = extract(reqBody, "password");

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
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                String reqBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    int id = Integer.parseInt(extract(reqBody, "id"));
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
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                String reqBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    int id = Integer.parseInt(extract(reqBody, "id"));
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
            String priority = extract(body, "priority");
            if (priority.isEmpty())
                priority = "MEDIUM";

            String desc = extract(body, "desc");
            if (desc.isEmpty())
                desc = extract(body, "description");

            String name = extract(body, "name");
            String status = extract(body, "status");
            String endDate = extract(body, "endDate");

            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int currentUserId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

            if (!new PermissionService().isManager(currentUserId, pid)) {
                sendResponse(ex, 403,
                        "{\"error\":\"Truy cập bị từ chối! Chỉ Trưởng dự án hoặc Quản lý mới được sửa.\"}");
                return;
            }

            boolean ok = new ProjectDAO().updateProject(pid, name, desc, status, endDate, priority);
            sendResponse(ex, ok ? 200 : 400, ok ? "{\"msg\":\"Xong\"}" : "{\"error\":\"Lỗi CSDL\"}");
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
            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int currentUserId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

            if (!new PermissionService().isOwner(currentUserId, pid)) {
                sendResponse(ex, 403, "{\"error\":\"Chỉ có Người tạo dự án (OWNER) mới có quyền xóa vĩnh viễn!\"}");
                return;
            }

            boolean ok = new ProjectDAO().deleteProject(pid);
            if (ok) {
                com.teamwork.kernel.PluginManager.notifyProjectDeleted(pid);
                sendResponse(ex, 200, "{\"msg\":\"Xóa xong\"}");
            } else {
                sendResponse(ex, 400, "{\"error\":\"Lỗi CSDL\"}");
            }
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

            String identifier = extract(body, "identifier");
            if (identifier.isEmpty())
                identifier = extract(body, "email");
            identifier = identifier.trim();
            if (identifier.startsWith("@"))
                identifier = identifier.substring(1).trim();

            String role = extract(body, "role");
            String projectName = extract(body, "projectName");
            String inviterName = extract(body, "inviterName");

            if (projectName.isEmpty())
                projectName = "Dự án #" + projectId;
            if (inviterName.isEmpty())
                inviterName = "Quản lý dự án";

            String result = new ProjectMemberDAO().inviteMember(projectId, projectName, identifier, role, inviterName);

            if ("SUCCESS".equals(result)) {
                sendResponse(ex, 200, "{\"message\":\"Đã gửi lời mời và Email thành công!\"}");
            } else {
                sendResponse(ex, 400, "{\"error\":\"" + result + "\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleSearchUsers(HttpExchange ex) throws java.io.IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String query = ex.getRequestURI().getQuery();
            String keyword = "";
            if (query != null && query.contains("q=")) {
                keyword = query.split("q=")[1].split("&")[0];
                keyword = java.net.URLDecoder.decode(keyword, StandardCharsets.UTF_8.name());
            }
            sendResponse(ex, 200, userDAO.searchUsers(keyword));
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleGetNotifications(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int currentUserId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;
            sendResponse(ex, 200, new NotificationDAO().getUserNotifications(currentUserId));
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleRespondInvite(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int notifId = Integer.parseInt(extract(body, "notificationId"));
            int projectId = Integer.parseInt(extract(body, "projectId"));
            boolean isAccept = Boolean.parseBoolean(extract(body, "isAccept"));

            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int currentUserId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

            boolean ok = new ProjectMemberDAO().respondToInvite(currentUserId, projectId, isAccept);
            if (ok) {
                new NotificationDAO().markAsRead(notifId);
                sendResponse(ex, 200, "{\"message\":\"Đã xử lý lời mời!\"}");
            } else {
                sendResponse(ex, 400, "{\"error\":\"Lỗi xử lý hệ thống!\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleCors(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, User-ID");
    }

    private String extract(String json, String key) {
        Matcher mString = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (mString.find())
            return mString.group(1).trim();

        Matcher mNum = Pattern.compile("\"" + key + "\"\\s*:\\s*([a-zA-Z0-9\\.]+)").matcher(json);
        if (mNum.find())
            return mNum.group(1).trim();

        return "";
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