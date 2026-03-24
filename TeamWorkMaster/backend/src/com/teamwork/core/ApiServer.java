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
        server.createContext("/api/projects/update-role", this::handleUpdateMemberRole);
        server.createContext("/api/projects/remove-member", this::handleRemoveMember);

        server.createContext("/api/tasks/delete", this::handleTaskDelete);
        server.createContext("/api/tasks/update-details", this::handleTaskUpdateDetails);
        server.createContext("/api/tasks/list", this::handleTaskList);
        server.createContext("/api/tasks/create", this::handleTaskCreate);
        server.createContext("/api/tasks/update-status", this::handleTaskUpdateStatus);

        server.createContext("/api/tasks/subtasks", new SubtasksHandler());
        server.createContext("/api/tasks/logs", new TaskLogsHandler()); // 🟢 API MỚI: LẤY LOG
        server.createContext("/api/tasks/update-order", this::handleTaskUpdateOrder);
        server.createContext("/api/tasks/attachments", new TaskAttachmentsHandler());

        server.createContext("/api/comments", new CommentsHandler());
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

        server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println(">>> BACKEND ĐANG CHẠY TẠI CỔNG " + server.getAddress().getPort());
        System.out.println(">>> TRẠNG THÁI: SẴN SÀNG KẾT NỐI VỚI VUE");
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

    // ==========================================
    // 🟢 CLASS MỚI: TRẢ VỀ LỊCH SỬ HOẠT ĐỘNG
    // ==========================================
    class TaskLogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    int taskId = Integer.parseInt(extractQuery(exchange.getRequestURI().getQuery(), "taskId"));
                    sendResponse(exchange, 200, taskDAO.getTaskLogs(taskId));
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    class SubtasksHandler implements HttpHandler {
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
                    int taskId = Integer.parseInt(extractQuery(exchange.getRequestURI().getQuery(), "taskId"));
                    sendResponse(exchange, 200, taskDAO.getSubtasks(taskId));
                } else if ("POST".equals(method)) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    int taskId = Integer.parseInt(extract(body, "taskId"));
                    String title = extract(body, "title");
                    if (taskDAO.addSubtask(taskId, title))
                        sendResponse(exchange, 200, "{\"success\":true}");
                    else
                        sendResponse(exchange, 400, "{\"success\":false}");
                } else if ("PUT".equals(method)) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    int subtaskId = Integer.parseInt(extract(body, "subtaskId"));
                    boolean isCompleted = Boolean.parseBoolean(extract(body, "isCompleted"));
                    if (taskDAO.toggleSubtask(subtaskId, isCompleted))
                        sendResponse(exchange, 200, "{\"success\":true}");
                    else
                        sendResponse(exchange, 400, "{\"success\":false}");
                } else if ("DELETE".equals(method)) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    int subtaskId = Integer.parseInt(extract(body, "subtaskId"));
                    if (taskDAO.deleteSubtask(subtaskId))
                        sendResponse(exchange, 200, "{\"success\":true}");
                    else
                        sendResponse(exchange, 400, "{\"success\":false}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    class StatisticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                // 🟢 Lấy projectId từ thanh địa chỉ: /api/statistics?projectId=X
                int projectId = 0;
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("projectId=")) {
                    projectId = Integer.parseInt(query.split("projectId=")[1].split("&")[0]);
                }

                int[] stats = taskDAO.getTaskStatistics(projectId); // 🟢 Truyền projectId vào DAO
                String json = String.format("{\"todo\": %d, \"inProgress\": %d, \"done\": %d}", stats[0], stats[1],
                        stats[2]);
                sendResponse(exchange, 200, json);
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

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
                    int taskId = 0;
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null && query.contains("taskId="))
                        taskId = Integer.parseInt(query.split("taskId=")[1].split("&")[0]);

                    List<Comment> list = commentDAO.getCommentsByTask(taskId);
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        Comment c = list.get(i);
                        String safeFileUrl = (c.getFileUrl() != null) ? escapeJson(c.getFileUrl()) : "";
                        json.append(String.format(
                                "{\"id\":\"%s\",\"user\":\"%s\",\"content\":\"%s\",\"fileUrl\":\"%s\",\"time\":\"%s\"}",
                                escapeJson(String.valueOf(c.getId())), escapeJson(c.getUser()),
                                escapeJson(c.getContent()), safeFileUrl, escapeJson(c.getTime())));
                        if (i < list.size() - 1)
                            json.append(",");
                    }
                    json.append("]");
                    sendResponse(exchange, 200, json.toString());

                } else if ("POST".equals(method)) {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    int taskId = Integer.parseInt(extract(requestBody, "taskId"));
                    String content = extract(requestBody, "content");
                    String fileUrl = extract(requestBody, "fileUrl");

                    String userIdStr = exchange.getRequestHeaders().getFirst("User-ID");
                    int userId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

                    if (userId > 0 && commentDAO.addComment(taskId, userId, content, fileUrl)) {
                        sendResponse(exchange, 200, "{\"success\": true}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Lỗi thêm bình luận\"}");
                    }
                } else {
                    sendResponse(exchange, 405, "{\"message\": \"Method Not Allowed\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            }
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
                    int result = userDAO.addUserByAdmin(extract(reqBody, "username"), extract(reqBody, "password"),
                            extract(reqBody, "fullname"), extract(reqBody, "email"));
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
                    int result = userDAO.updateUser(Integer.parseInt(extract(reqBody, "id")),
                            extract(reqBody, "fullname"), extract(reqBody, "email"), extract(reqBody, "password"));
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
                    if (userDAO.deleteUser(Integer.parseInt(extract(reqBody, "id"))))
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
                    if (userDAO.toggleUserLock(Integer.parseInt(extract(reqBody, "id"))))
                        sendResponse(exchange, 200, "{\"success\": true}");
                    else
                        sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Lỗi cập nhật trạng thái!\"}");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ\"}");
                }
            }
        }
    }

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
                    int projectId = 0;
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null && query.contains("projectId="))
                        projectId = Integer.parseInt(query.split("projectId=")[1].split("&")[0]);
                    sendResponse(exchange, 200, chatDAO.getMessages(projectId));

                } else if ("POST".equals(method)) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    int projectId = Integer.parseInt(extract(body, "projectId"));
                    String content = extract(body, "content");
                    String fileUrl = extract(body, "fileUrl");

                    String userIdStr = exchange.getRequestHeaders().getFirst("User-ID");
                    int userId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

                    if (userId > 0 && chatDAO.addMessage(projectId, userId, content, fileUrl))
                        sendResponse(exchange, 200, "{\"success\": true}");
                    else
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Lỗi thêm tin nhắn\"}");

                } else if ("DELETE".equals(method)) {
                    int messageId = 0;
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null && query.contains("messageId="))
                        messageId = Integer.parseInt(query.split("messageId=")[1].split("&")[0]);

                    String userIdStr = exchange.getRequestHeaders().getFirst("User-ID");
                    int userId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

                    if (userId > 0 && chatDAO.deleteMessage(messageId, userId))
                        sendResponse(exchange, 200, "{\"success\": true}");
                    else
                        sendResponse(exchange, 400,
                                "{\"success\": false, \"message\": \"Không thể thu hồi tin nhắn\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            }
        }
    }

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
                    if ("current".equals(extractQuery(query, "type")))
                        sendResponse(exchange, 200, meetingDAO.getCurrentMeeting(projectId));
                    else
                        sendResponse(exchange, 200, meetingDAO.getMeetingHistory(projectId));
                } else if ("POST".equals(method)) {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    int projectId = Integer.parseInt(extract(requestBody, "projectId"));
                    String meetLink = extract(requestBody, "meetLink");
                    String projectName = extract(requestBody, "projectName");
                    String hostName = extract(requestBody, "hostName");
                    int hostId = Integer.parseInt(exchange.getRequestHeaders().getFirst("User-ID"));

                    int newMeetingId = meetingDAO.startMeeting(projectId, hostId, meetLink);
                    if (newMeetingId > 0) {
                        try {
                            ResultSet members = meetingDAO.getProjectMembersForNotification(projectId);
                            String notifTitle = "Họp Dự Án: " + projectName;
                            String notifContent = hostName
                                    + " vừa mở phòng họp. Mời bạn vào mục Phòng Họp Nhóm để tham gia.";
                            String emailSubject = "[Teamwork] Mời họp trực tuyến - Dự án: " + projectName;
                            String emailContent = "Xin chào,\n\nQuản lý " + hostName + " vừa mở phòng họp cho dự án "
                                    + projectName + ".\n\n👉 Vui lòng click vào đường link sau để tham gia ngay: "
                                    + meetLink + "\n\nTrân trọng,\nHệ thống Teamwork Master";

                            NotificationDAO notifDAO = new NotificationDAO();
                            while (members.next()) {
                                int memberId = members.getInt("ID");
                                String memberEmail = members.getString("Email");
                                if (memberId != hostId) {
                                    notifDAO.addNotification(memberId, projectId, notifTitle, notifContent);
                                    new Thread(() -> {
                                        try {
                                            com.teamwork.plugins.EmailService.sendEmail(memberEmail, emailSubject,
                                                    emailContent);
                                        } catch (Exception e) {
                                        }
                                    }).start();
                                }
                            }
                        } catch (Exception e) {
                        }
                        sendResponse(exchange, 200, "{\"success\": true, \"meetingId\": " + newMeetingId + "}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\": false}");
                    }
                } else if ("PUT".equals(method)) {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    String action = extract(requestBody, "action");
                    int meetingId = Integer.parseInt(extract(requestBody, "meetingId"));
                    String notes = extract(requestBody, "notes");

                    boolean success = false;
                    if ("end".equals(action))
                        success = meetingDAO.endMeeting(meetingId, notes);
                    else if ("updateNotes".equals(action))
                        success = meetingDAO.updateMeetingNotes(meetingId, notes);

                    if (success)
                        sendResponse(exchange, 200, "{\"success\": true}");
                    else
                        sendResponse(exchange, 400, "{\"success\": false}");
                }
            } catch (Exception e) {
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
            sendResponse(ex, 200, new ProjectMemberDAO()
                    .getProjectMembersJson(Integer.parseInt(extractQuery(ex.getRequestURI().getQuery(), "projectId"))));
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
        try {
            if (taskDAO.deleteTask(Integer.parseInt(
                    extract(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), "taskId"))))
                sendResponse(ex, 200, "{\"message\":\"Đã xóa thẻ thành công\"}");
            else
                sendResponse(ex, 400, "{\"error\":\"Lỗi CSDL khi xóa\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // 🟢 ĐÃ CẬP NHẬT: Ghi log khi Sửa công việc
    private void handleTaskUpdateDetails(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int taskId = Integer.parseInt(extract(body, "taskId"));
            String title = extract(body, "title");
            String newDeadline = extract(body, "deadline");
            String assigneeIds = extract(body, "assigneeIds");

            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int currentUserId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 1;

            // =========================================================
            // 🛡️ BƯỚC 1: KIỂM TRA QUYỀN TRƯỚC KHI CHO PHÉP SỬA
            // =========================================================
            String oldDeadline = "null";
            String projectName = "Dự án";
            String uName = "Một quản lý";
            int projectId = 0;

            try {
                java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();

                String sql = "SELECT t.ProjectID, t.Deadline, p.ProjectName FROM TBL_TASKS t JOIN TBL_PROJECTS p ON t.ProjectID = p.ID WHERE t.ID=?";
                try (java.sql.PreparedStatement p1 = conn.prepareStatement(sql)) {
                    p1.setInt(1, taskId);
                    try (java.sql.ResultSet rs1 = p1.executeQuery()) {
                        if (rs1.next()) {
                            projectId = rs1.getInt("ProjectID");
                            java.sql.Date d = rs1.getDate("Deadline");
                            oldDeadline = (d != null) ? d.toString() : "null";
                            projectName = rs1.getString("ProjectName");
                        }
                    }
                }

                // 🛑 CHỐT CHẶN BẢO MẬT
                if (!new PermissionService().isManager(currentUserId, projectId)) {
                    sendResponse(ex, 403,
                            "{\"error\":\"Truy cập bị từ chối! Chỉ Quản lý hoặc Trưởng dự án mới được sửa công việc.\"}");
                    return;
                }

                try (java.sql.PreparedStatement p2 = conn
                        .prepareStatement("SELECT FullName FROM TBL_USERS WHERE ID=?")) {
                    p2.setInt(1, currentUserId);
                    try (java.sql.ResultSet rs2 = p2.executeQuery()) {
                        if (rs2.next())
                            uName = rs2.getString("FullName");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // =========================================================
            // 🚀 BƯỚC 2: TIẾN HÀNH CẬP NHẬT & BẮN EMAIL
            // =========================================================
            if (taskDAO.updateTaskDetails(taskId, title, extract(body, "description"), extract(body, "priority"),
                    newDeadline, extract(body, "startDate"), extract(body, "tags"), assigneeIds)) {

                taskDAO.addTaskLog(taskId, currentUserId, "Đã cập nhật chi tiết công việc");

                // 🟢 BẮN EMAIL VÀ LƯU THÔNG BÁO
                String checkNewDl = (newDeadline == null || newDeadline.trim().isEmpty()) ? "null" : newDeadline;
                if (!oldDeadline.equals(checkNewDl)) {
                    final int fProjectId = projectId; // 🟢 Lấy Project ID
                    final String fProjectName = projectName;
                    final String fOldDl = oldDeadline;
                    final String fNewDl = checkNewDl;
                    final String fUName = uName;
                    new Thread(() -> {
                        // 🟢 Truyền fProjectId vào hàm
                        com.teamwork.plugins.TaskActionNotifier.notifyDeadlineChanged(fProjectId, fProjectName, title,
                                assigneeIds,
                                fOldDl, fNewDl, fUName);
                    }).start();
                }

                sendResponse(ex, 200, "{\"message\":\"Cập nhật thành công\"}");
            } else {
                sendResponse(ex, 400, "{\"error\":\"Lỗi CSDL khi cập nhật\"}");
            }
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
            sendResponse(ex, 200, taskDAO
                    .getTasksByProject(Integer.parseInt(extractQuery(ex.getRequestURI().getQuery(), "projectId"))));
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
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int projectId = Integer.parseInt(extract(body, "projectId")); // Đã có Project ID ở đây
            String title = extract(body, "title");
            String assigneeIds = extract(body, "assigneeIds");
            String priority = extract(body, "priority").isEmpty() ? "MEDIUM" : extract(body, "priority");
            String status = extract(body, "targetColumn").isEmpty() ? "TODO" : extract(body, "targetColumn");

            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int assignerId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 1;

            if (taskDAO.createTask(projectId, title, extract(body, "description"), priority, extract(body, "deadline"),
                    extract(body, "startDate"), extract(body, "tags"), status, assigneeIds)) {

                System.out.println("✅ [API-Task] Tạo thẻ thành công trong DB. Bắt đầu kích hoạt Email...");
                System.out.println("👉 Giá trị assigneeIds nhận được từ Web: [" + assigneeIds + "]");

                // 🟢 BẮN EMAIL VÀ LƯU THÔNG BÁO
                new Thread(() -> {
                    try {
                        java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();
                        String pName = "Dự án";
                        String uName = "Quản lý";

                        try (java.sql.PreparedStatement p1 = conn
                                .prepareStatement("SELECT ProjectName FROM TBL_PROJECTS WHERE ID=?")) {
                            p1.setInt(1, projectId);
                            try (java.sql.ResultSet rs1 = p1.executeQuery()) {
                                if (rs1.next())
                                    pName = rs1.getString("ProjectName");
                            }
                        }
                        try (java.sql.PreparedStatement p2 = conn
                                .prepareStatement("SELECT FullName FROM TBL_USERS WHERE ID=?")) {
                            p2.setInt(1, assignerId);
                            try (java.sql.ResultSet rs2 = p2.executeQuery()) {
                                if (rs2.next())
                                    uName = rs2.getString("FullName");
                            }
                        }
                        // 🟢 Truyền projectId vào hàm
                        com.teamwork.plugins.TaskActionNotifier.notifyNewTask(projectId, pName, title, assigneeIds,
                                uName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                sendResponse(ex, 201, "{\"message\":\"Tạo công việc thành công\"}");
            } else
                sendResponse(ex, 400, "{\"error\":\"Tạo thất bại do lỗi CSDL\"}");
        } catch (Exception e) {
            sendResponse(ex, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // 🟢 ĐÃ CẬP NHẬT: Ghi log khi Kéo thả (Đổi trạng thái)
    private void handleTaskUpdateStatus(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int taskId = Integer.parseInt(extract(body, "taskId"));
            String status = extract(body, "status");
            String oldStatus = extract(body, "oldStatus");

            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int currentUserId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

            if (taskDAO.updateTaskStatus(taskId, status)) {
                String oldName = oldStatus.equals("TODO") ? "Cần làm"
                        : oldStatus.equals("IN_PROGRESS") ? "Đang thực hiện"
                                : oldStatus.equals("DONE") ? "Đã hoàn thành" : "Đã hủy";
                String newName = status.equals("TODO") ? "Cần làm"
                        : status.equals("IN_PROGRESS") ? "Đang thực hiện"
                                : status.equals("DONE") ? "Đã hoàn thành" : "Đã hủy";
                taskDAO.addTaskLog(taskId, currentUserId, "Đã chuyển thẻ từ: " + oldName + " ➡️ " + newName);

                // 🟢 BẮN EMAIL: BÁO CÁO HOÀN THÀNH CHO SẾP
                // 🟢 BẮN EMAIL: BÁO CÁO HOÀN THÀNH CHO SẾP
                if ("DONE".equals(status) && !"DONE".equals(oldStatus)) {
                    new Thread(() -> {
                        try {
                            java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();
                            String tTitle = "";
                            int pId = 0;
                            String uName = "Một thành viên";

                            try (java.sql.PreparedStatement p1 = conn
                                    .prepareStatement("SELECT Title, ProjectID FROM TBL_TASKS WHERE ID=?")) {
                                p1.setInt(1, taskId);
                                try (java.sql.ResultSet rs1 = p1.executeQuery()) {
                                    if (rs1.next()) {
                                        tTitle = rs1.getString("Title");
                                        pId = rs1.getInt("ProjectID");
                                    }
                                }
                            }
                            try (java.sql.PreparedStatement p2 = conn
                                    .prepareStatement("SELECT FullName FROM TBL_USERS WHERE ID=?")) {
                                p2.setInt(1, currentUserId);
                                try (java.sql.ResultSet rs2 = p2.executeQuery()) {
                                    if (rs2.next())
                                        uName = rs2.getString("FullName");
                                }
                            }
                            // Truyền thêm currentUserId vào để nó đem đi so sánh
                            com.teamwork.plugins.TaskActionNotifier.notifyTaskCompleted(pId, tTitle, currentUserId,
                                    uName);
                                                                                                                    
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }

                sendResponse(ex, 200, "{\"message\":\"Cập nhật vị trí thành công\"}");
            } else
                sendResponse(ex, 400, "{\"error\":\"Lỗi CSDL khi cập nhật\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleTaskUpdateOrder(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String taskIdsStr = extract(body, "taskIds"); // Nhận chuỗi "15,22,9,3"

            if (!taskIdsStr.isEmpty()) {
                String[] ids = taskIdsStr.split(",");
                for (int i = 0; i < ids.length; i++) {
                    if (!ids[i].trim().isEmpty()) {
                        taskDAO.updateTaskOrder(Integer.parseInt(ids[i].trim()), i); // i chính là thứ tự 0, 1, 2, 3...
                    }
                }
            }
            sendResponse(ex, 200, "{\"message\":\"Cập nhật thứ tự thành công\"}");
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
            sendResponse(ex, 200, new ProjectDAO()
                    .getAllProjectsJson((userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0));
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
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String desc = extract(body, "desc").isEmpty() ? extract(body, "description") : extract(body, "desc");
            String priority = extract(body, "priority").isEmpty() ? "MEDIUM" : extract(body, "priority");
            String name = extract(body, "name");
            int userId = Integer.parseInt(extract(body, "userId"));

            int pid = new ProjectDAO().createProject(name, desc, extract(body, "startDate"), extract(body, "endDate"),
                    priority, userId);
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

    private void handleUpdate(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int pid = Integer.parseInt(extract(body, "projectId"));
            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int currentUserId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

            if (!new PermissionService().isManager(currentUserId, pid)) {
                sendResponse(ex, 403,
                        "{\"error\":\"Truy cập bị từ chối! Chỉ Trưởng dự án hoặc Quản lý mới được sửa.\"}");
                return;
            }
            String priority = extract(body, "priority").isEmpty() ? "MEDIUM" : extract(body, "priority");
            String desc = extract(body, "desc").isEmpty() ? extract(body, "description") : extract(body, "desc");

            if (new ProjectDAO().updateProject(pid, extract(body, "name"), desc, extract(body, "status"),
                    extract(body, "endDate"), priority))
                sendResponse(ex, 200, "{\"msg\":\"Xong\"}");
            else
                sendResponse(ex, 400, "{\"error\":\"Lỗi CSDL\"}");
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
        try {
            int pid = Integer.parseInt(
                    extract(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), "projectId"));
            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int currentUserId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

            if (!new PermissionService().isOwner(currentUserId, pid)) {
                sendResponse(ex, 403, "{\"error\":\"Chỉ có Người tạo dự án (OWNER) mới có quyền xóa vĩnh viễn!\"}");
                return;
            }
            if (new ProjectDAO().deleteProject(pid)) {
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
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int projectId = Integer.parseInt(extract(body, "projectId"));
            String identifier = extract(body, "identifier").isEmpty() ? extract(body, "email")
                    : extract(body, "identifier");
            identifier = identifier.trim();
            if (identifier.startsWith("@"))
                identifier = identifier.substring(1).trim();

            String projectName = extract(body, "projectName").isEmpty() ? "Dự án #" + projectId
                    : extract(body, "projectName");
            String inviterName = extract(body, "inviterName").isEmpty() ? "Quản lý dự án"
                    : extract(body, "inviterName");

            String result = new ProjectMemberDAO().inviteMember(projectId, projectName, identifier,
                    extract(body, "role"), inviterName);

            if ("SUCCESS".equals(result)) {
                if (identifier.contains("@") && identifier.contains(".")) {
                    final String targetEmail = identifier;
                    final String pName = projectName;
                    final String iName = inviterName;
                    new Thread(() -> {
                        try {
                            String emailContent = "Xin chào,\n\n" + iName + " vừa mời bạn tham gia vào dự án " + pName
                                    + ".\nVui lòng đăng nhập vào hệ thống để Đồng ý hoặc Từ chối.\n\nTrân trọng,\nHệ thống Teamwork Master";
                            com.teamwork.plugins.EmailService.sendEmail(targetEmail,
                                    "[Teamwork] Lời mời tham gia dự án: " + pName, emailContent);
                        } catch (Exception e) {
                        }
                    }).start();
                }
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
            String keyword = extractQuery(ex.getRequestURI().getQuery(), "q");
            if (!keyword.isEmpty())
                keyword = java.net.URLDecoder.decode(keyword, StandardCharsets.UTF_8.name());
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
            sendResponse(ex, 200, new NotificationDAO().getUserNotifications(
                    (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0));
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
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int notifId = Integer.parseInt(extract(body, "notificationId"));

            if (new ProjectMemberDAO().respondToInvite(
                    (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0,
                    Integer.parseInt(extract(body, "projectId")), Boolean.parseBoolean(extract(body, "isAccept")))) {
                new NotificationDAO().markAsRead(notifId);
                sendResponse(ex, 200, "{\"message\":\"Đã xử lý lời mời!\"}");
            } else {
                sendResponse(ex, 400, "{\"error\":\"Lỗi xử lý hệ thống!\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleUpdateMemberRole(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (new ProjectMemberDAO().updateRole(Integer.parseInt(extract(body, "projectId")),
                    Integer.parseInt(extract(body, "targetUserId")), extract(body, "newRole")))
                sendResponse(ex, 200, "{\"success\": true, \"message\": \"Đã cập nhật quyền thành công!\"}");
            else
                sendResponse(ex, 400, "{\"success\": false, \"error\": \"Lỗi khi cập nhật quyền\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handleRemoveMember(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (new ProjectMemberDAO().removeMember(Integer.parseInt(extract(body, "projectId")),
                    Integer.parseInt(extract(body, "targetUserId"))))
                sendResponse(ex, 200, "{\"success\": true, \"message\": \"Đã xóa thành viên khỏi dự án!\"}");
            else
                sendResponse(ex, 400, "{\"success\": false, \"error\": \"Lỗi khi xóa thành viên\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // ==========================================
    // 🟢 CLASS MỚI: API TÀI LIỆU ĐÍNH KÈM
    // ==========================================
    class TaskAttachmentsHandler implements HttpHandler {
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
                    int taskId = Integer.parseInt(extractQuery(exchange.getRequestURI().getQuery(), "taskId"));
                    sendResponse(exchange, 200, taskDAO.getTaskAttachments(taskId));
                } else if ("POST".equals(method)) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    int taskId = Integer.parseInt(extract(body, "taskId"));
                    String fileName = extract(body, "fileName");
                    String fileUrl = extract(body, "fileUrl");

                    String userIdStr = exchange.getRequestHeaders().getFirst("User-ID");
                    int userId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

                    if (taskDAO.addTaskAttachment(taskId, userId, fileName, fileUrl)) {
                        // Ghi Log lịch sử
                        taskDAO.addTaskLog(taskId, userId, "Đã đính kèm tài liệu: " + fileName);
                        sendResponse(exchange, 200, "{\"success\":true}");
                    } else
                        sendResponse(exchange, 400, "{\"success\":false}");
                } else if ("DELETE".equals(method)) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    int attachmentId = Integer.parseInt(extract(body, "attachmentId"));
                    if (taskDAO.deleteTaskAttachment(attachmentId))
                        sendResponse(exchange, 200, "{\"success\":true}");
                    else
                        sendResponse(exchange, 400, "{\"success\":false}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }
}