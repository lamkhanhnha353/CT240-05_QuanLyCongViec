package com.teamwork.core;

import com.sun.net.httpserver.*;
import com.teamwork.db.*;
import com.teamwork.model.Comment;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.List;
import java.util.regex.*;
import com.teamwork.service.PermissionService;

public class ApiServer {
    private HttpServer server;
    private UserDAO userDAO;
    private TaskDAO taskDAO;
    private CommentDAO commentDAO;

    public ApiServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        userDAO = new UserDAO();
        taskDAO = new TaskDAO();
        commentDAO = new CommentDAO();

        server.createContext("/api/projects/list", this::handleList);
        server.createContext("/api/projects/create", this::handleCreate);
        server.createContext("/api/projects/update", this::handleUpdate);
        server.createContext("/api/projects/delete", this::handleDelete);
        server.createContext("/api/projects/add-member", this::handleAddMember);

        server.createContext("/api/projects/members-list", this::handleProjectMembersList);
        server.createContext("/api/tasks/delete", this::handleTaskDelete);
        server.createContext("/api/tasks/update-details", this::handleTaskUpdateDetails);
        server.createContext("/api/tasks/list", this::handleTaskList);
        server.createContext("/api/tasks/create", this::handleTaskCreate);
        server.createContext("/api/tasks/update-status", this::handleTaskUpdateStatus);

        server.createContext("/api/comments", new CommentsHandler());

        // --- ENDPOINT THỐNG KÊ CỦA ĐỒNG ĐỘI ---
        server.createContext("/api/statistics", new StatisticsHandler());

        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());

        server.createContext("/api/admin/users/create", new AdminCreateUserHandler());
        server.createContext("/api/admin/users", new AdminGetUsersHandler());
        server.createContext("/api/admin/users/update", new AdminUpdateUserHandler());
        server.createContext("/api/admin/users/delete", new AdminDeleteUserHandler());
        server.createContext("/api/admin/users/toggle-lock", new AdminToggleLockUserHandler());

        server.createContext("/api/users/search", this::handleSearchUsers);
        server.createContext("/api/notifications/list", this::handleGetNotifications);
        server.createContext("/api/notifications/respond", this::handleRespondInvite);


        server.createContext("/api/project-chat", new ProjectChatHandler());
        server.createContext("/api/meetings", new MeetingHandler());
        server.createContext("/api/projects/update-role", this::handleUpdateMemberRole);
        server.createContext("/api/projects/remove-member", this::handleRemoveMember);
        server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println(">>> BACKEND ĐANG CHẠY TẠI CỔNG " + server.getAddress().getPort());
        System.out.println(">>> TRẠNG THÁI: SẴN SÀNG KẾT NỐI VỚI VUE");
    }

    // ==========================================
    // API THỐNG KÊ (Đã tích hợp)
    // ==========================================
    class StatisticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                int[] stats = taskDAO.getTaskStatistics();
                // Trả về JSON chuẩn để Vue.js dễ vẽ biểu đồ
                String json = String.format("{\"todo\": %d, \"inProgress\": %d, \"done\": %d}", stats[0], stats[1],
                        stats[2]);
                sendResponse(exchange, 200, json);
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // ==========================================
    // API BÌNH LUẬN
    // ==========================================
    class CommentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                if ("GET".equals(method)) {
                    String query = exchange.getRequestURI().getQuery();
                    int taskId = 0;
                    if (query != null && query.contains("taskId=")) {
                        taskId = Integer.parseInt(query.split("taskId=")[1].split("&")[0]);
                    }

                    List<Comment> list = commentDAO.getCommentsByTask(taskId);
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        Comment c = list.get(i);
                        json.append(String.format("{\"id\":\"%s\",\"user\":\"%s\",\"content\":\"%s\",\"time\":\"%s\"}",
                                escapeJson(c.getId()), escapeJson(c.getUser()), escapeJson(c.getContent()),
                                escapeJson(c.getTime())));
                        if (i < list.size() - 1)
                            json.append(",");
                    }
                    json.append("]");
                    sendResponse(exchange, 200, json.toString());

                } else if ("POST".equals(method)) {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    int taskId = Integer.parseInt(extract(requestBody, "taskId"));
                    String content = extract(requestBody, "content");

                    String userIdStr = exchange.getRequestHeaders().getFirst("User-ID");
                    int userId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

                    if (userId > 0 && commentDAO.addComment(taskId, userId, content)) {
                        sendResponse(exchange, 200, "{\"success\": true}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Lỗi thêm bình luận\"}");
                    }
                } else {
                    sendResponse(exchange, 405, "{\"message\": \"Method Not Allowed\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            }
        }
    }

    private void handleProjectMembersList(HttpExchange ex) throws IOException {
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
            sendResponse(ex, 200, new ProjectMemberDAO().getProjectMembersJson(projectId));
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleTaskDelete(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int taskId = Integer.parseInt(extract(body, "taskId"));
            boolean ok = taskDAO.deleteTask(taskId);
            if (ok)
                sendResponse(ex, 200, "{\"message\":\"Đã xóa thẻ thành công\"}");
            else
                sendResponse(ex, 400, "{\"error\":\"Lỗi CSDL khi xóa\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleTaskUpdateDetails(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int taskId = Integer.parseInt(extract(body, "taskId"));
            String title = extract(body, "title");
            String desc = extract(body, "description");
            if (desc.isEmpty())
                desc = extract(body, "desc");
            String priority = extract(body, "priority");
            String deadline = extract(body, "deadline");
            String assigneeStr = extract(body, "assigneeId");
            int assigneeId = assigneeStr.isEmpty() ? 0 : Integer.parseInt(assigneeStr);

            boolean ok = taskDAO.updateTaskDetails(taskId, title, desc, priority, deadline, assigneeId);
            if (ok)
                sendResponse(ex, 200, "{\"message\":\"Cập nhật thành công\"}");
            else
                sendResponse(ex, 400, "{\"error\":\"Lỗi CSDL khi cập nhật\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleTaskList(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String query = ex.getRequestURI().getQuery();
            int projectId = 0;
            if (query != null && query.contains("projectId="))
                projectId = Integer.parseInt(query.split("projectId=")[1].split("&")[0]);
            sendResponse(ex, 200, taskDAO.getTasksByProject(projectId));
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

            boolean ok = taskDAO.createTask(projectId, title, desc, priority, deadline, assigneeId);
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

            boolean ok = taskDAO.updateTaskStatus(taskId, status);
            if (ok)
                sendResponse(ex, 200, "{\"message\":\"Cập nhật vị trí thành công\"}");
            else
                sendResponse(ex, 400, "{\"error\":\"Lỗi CSDL khi cập nhật\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

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
                        String[] parts = loginResult.split("-", 3);
                        sendResponse(exchange, 200,
                                "{\"success\": true, \"userId\": " + parts[0] + ", \"role\": \"" + parts[1]
                                        + "\", \"fullName\": \"" + escapeJson(parts[2]) + "\"}");
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

    // ==========================================
    // API CHAT TỔNG DỰ ÁN (CÓ HỖ TRỢ FILE)
    // ==========================================
    // ==========================================
    // API CHAT TỔNG DỰ ÁN (CÓ HỖ TRỢ FILE & XÓA)
    // ==========================================
    class ProjectChatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            ProjectChatDAO chatDAO = new ProjectChatDAO();
            try {
                if ("GET".equals(method)) {
                    String query = exchange.getRequestURI().getQuery();
                    int projectId = 0;
                    if (query != null && query.contains("projectId=")) {
                        projectId = Integer.parseInt(query.split("projectId=")[1].split("&")[0]);
                    }
                    sendResponse(exchange, 200, chatDAO.getMessages(projectId));

                } else if ("POST".equals(method)) {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                    int projectId = Integer.parseInt(extract(requestBody, "projectId"));
                    String content = extract(requestBody, "content");
                    String fileUrl = extract(requestBody, "fileUrl");

                    String userIdStr = exchange.getRequestHeaders().getFirst("User-ID");
                    int userId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

                    if (userId > 0 && chatDAO.addMessage(projectId, userId, content, fileUrl)) {
                        sendResponse(exchange, 200, "{\"success\": true}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Lỗi thêm tin nhắn\"}");
                    }

                }
                // 🟢 THÊM ĐOẠN NÀY ĐỂ XỬ LÝ LỆNH THU HỒI TIN NHẮN 🟢
                else if ("DELETE".equals(method)) {
                    String query = exchange.getRequestURI().getQuery();
                    int messageId = 0;
                    if (query != null && query.contains("messageId=")) {
                        messageId = Integer.parseInt(query.split("messageId=")[1].split("&")[0]);
                    }

                    String userIdStr = exchange.getRequestHeaders().getFirst("User-ID");
                    int userId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

                    if (userId > 0 && chatDAO.deleteMessage(messageId, userId)) {
                        sendResponse(exchange, 200, "{\"success\": true}");
                    } else {
                        sendResponse(exchange, 400,
                                "{\"success\": false, \"message\": \"Không thể thu hồi tin nhắn\"}");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            }
        }
    }


    // cuộc họp meeting:
    // ==========================================
    // API QUẢN LÝ PHÒNG HỌP & LỊCH SỬ
    // ==========================================
    // ==========================================
    // API QUẢN LÝ PHÒNG HỌP & LỊCH SỬ (CÓ THÔNG BÁO)
    // ==========================================
    class MeetingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            MeetingDAO meetingDAO = new MeetingDAO();
            try {
                if ("GET".equals(method)) {
                    String query = exchange.getRequestURI().getQuery();
                    int projectId = Integer.parseInt(extractQuery(query, "projectId"));
                    String type = extractQuery(query, "type");

                    if ("current".equals(type)) {
                        sendResponse(exchange, 200, meetingDAO.getCurrentMeeting(projectId));
                    } else {
                        sendResponse(exchange, 200, meetingDAO.getMeetingHistory(projectId));
                    }
                } else if ("POST".equals(method)) {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    int projectId = Integer.parseInt(extract(requestBody, "projectId"));
                    String meetLink = extract(requestBody, "meetLink");

                    // Lấy thêm Tên dự án và Tên người mở phòng từ Frontend gửi lên
                    String projectName = extract(requestBody, "projectName");
                    String hostName = extract(requestBody, "hostName");

                    int hostId = Integer.parseInt(exchange.getRequestHeaders().getFirst("User-ID"));

                    int newMeetingId = meetingDAO.startMeeting(projectId, hostId, meetLink);
                    if (newMeetingId > 0) {

                        // 🔔 KÍCH HOẠT HỆ THỐNG THÔNG BÁO & EMAIL 🔔
                        try {
                            ResultSet members = meetingDAO.getProjectMembersForNotification(projectId);

                            // 1. Nội dung thông báo trên Web (Ngắn gọn, KHÔNG CÓ LINK)
                            String notifTitle = "Họp Dự Án: " + projectName;
                            String notifContent = hostName
                                    + " vừa mở phòng họp. Mời bạn vào mục Phòng Họp Nhóm để tham gia.";

                            // 2. Nội dung Email (Chi tiết, CÓ KÈM LINK GOOGLE MEET)
                            String emailSubject = "[Teamwork] Mời họp trực tuyến - Dự án: " + projectName;
                            String emailContent = "Xin chào,\n\nQuản lý " + hostName + " vừa mở phòng họp cho dự án "
                                    + projectName + ".\n\n"
                                    + "👉 Vui lòng click vào đường link sau để tham gia ngay: " + meetLink + "\n\n"
                                    + "Trân trọng,\nHệ thống Teamwork Master";

                            NotificationDAO notifDAO = new NotificationDAO();

                            while (members.next()) {
                                int memberId = members.getInt("ID");
                                String memberEmail = members.getString("Email"); // Lấy email từ DB

                                if (memberId != hostId) { // Không tự gửi cho chính mình

                                    // A. Bắn thông báo lên Chuông (Nhanh, đồng bộ)
                                    notifDAO.addNotification(memberId, projectId, notifTitle, notifContent);

                                    // B. Bắn Email qua SendGrid (Tạo một luồng chạy ngầm để không bị lag web)
                                    new Thread(() -> {
                                        com.teamwork.plugins.EmailService.sendEmail(memberEmail, emailSubject,
                                                emailContent);
                                    }).start();

                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Lỗi gửi thông báo/email: " + e.getMessage());
                        }

                        sendResponse(exchange, 200, "{\"success\": true, \"meetingId\": " + newMeetingId + "}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\": false}");
                    }
                } else if ("PUT".equals(method)) {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    String action = extract(requestBody, "action"); // Nhận diện hành động
                    int meetingId = Integer.parseInt(extract(requestBody, "meetingId"));
                    String notes = extract(requestBody, "notes");

                    boolean success = false;

                    // Nếu là hành động "Kết thúc họp"
                    if ("end".equals(action)) {
                        success = meetingDAO.endMeeting(meetingId, notes);
                    }
                    // Nếu là hành động "Cập nhật lịch sử biên bản"
                    else if ("updateNotes".equals(action)) {
                        success = meetingDAO.updateMeetingNotes(meetingId, notes);
                    }

                    if (success) {
                        sendResponse(exchange, 200, "{\"success\": true}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\": false}");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            }
        }

        private String extractQuery(String query, String key) {
            if (query == null)
                return "";
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1 && pair[0].equals(key))
                    return pair[1];
            }
            return "";
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

            // Gọi hàm bên DAO để xử lý DB và nhét thông báo Chuông
            String result = new ProjectMemberDAO().inviteMember(projectId, projectName, identifier, role, inviterName);

            if ("SUCCESS".equals(result)) {

                // ========================================================
                // 🟢 THÊM LOGIC BẮN EMAIL NẾU IDENTIFIER LÀ EMAIL HỢP LỆ 🟢
                // ========================================================
                // Kiểm tra xem chuỗi người dùng nhập vào có dạng email không (@xxx.com)
                if (identifier.contains("@") && identifier.contains(".")) {
                    final String targetEmail = identifier; // Tạo biến final để nhét vào Thread
                    final String pName = projectName;
                    final String iName = inviterName;

                    new Thread(() -> {
                        try {
                            String emailSubject = "[Teamwork] Lời mời tham gia dự án: " + pName;
                            String emailContent = "Xin chào,\n\n"
                                    + iName + " vừa mời bạn tham gia vào dự án " + pName
                                    + " trên hệ thống Teamwork Master.\n"
                                    + "Vui lòng đăng nhập vào hệ thống web để Đồng ý hoặc Từ chối lời mời này.\n\n"
                                    + "Trân trọng,\nHệ thống Teamwork Master";

                            com.teamwork.plugins.EmailService.sendEmail(targetEmail, emailSubject, emailContent);
                            System.out.println(">>> Đã bắn Email Lời mời dự án cho: " + targetEmail);
                        } catch (Exception e) {
                            System.out.println("Lỗi khi gửi email mời dự án: " + e.getMessage());
                        }
                    }).start();
                }
                // ========================================================

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

    private String escapeJson(String data) {
        if (data == null)
            return "";
        return data.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    // API: CẬP NHẬT QUYỀN THÀNH VIÊN
    private void handleUpdateMemberRole(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int projectId = Integer.parseInt(extract(body, "projectId"));
            int targetUserId = Integer.parseInt(extract(body, "targetUserId"));
            String newRole = extract(body, "newRole");

            // TODO: Ở hệ thống thực tế cần check quyền của người gửi request tại đây
            boolean ok = new ProjectMemberDAO().updateRole(projectId, targetUserId, newRole);
            if (ok) {
                sendResponse(ex, 200, "{\"success\": true, \"message\": \"Đã cập nhật quyền thành công!\"}");
            } else {
                sendResponse(ex, 400, "{\"success\": false, \"error\": \"Lỗi khi cập nhật quyền\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // API: XÓA THÀNH VIÊN HOẶC RỜI DỰ ÁN
    private void handleRemoveMember(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int projectId = Integer.parseInt(extract(body, "projectId"));
            int targetUserId = Integer.parseInt(extract(body, "targetUserId"));

            boolean ok = new ProjectMemberDAO().removeMember(projectId, targetUserId);
            if (ok) {
                sendResponse(ex, 200, "{\"success\": true, \"message\": \"Đã xóa thành viên khỏi dự án!\"}");
            } else {
                sendResponse(ex, 400, "{\"success\": false, \"error\": \"Lỗi khi xóa thành viên\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }
}