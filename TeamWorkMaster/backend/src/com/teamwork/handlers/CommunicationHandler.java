package com.teamwork.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.teamwork.db.*;
import com.teamwork.model.Comment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.List;

public class CommunicationHandler extends BaseHandler {
    private CommentDAO commentDAO = new CommentDAO();

    public void handleComments(HttpExchange exchange) throws IOException {
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
                            escapeJson(String.valueOf(c.getId())), escapeJson(c.getUser()), escapeJson(c.getContent()),
                            safeFileUrl, escapeJson(c.getTime())));
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

                if (userId > 0 && commentDAO.addComment(taskId, userId, content, fileUrl))
                    sendResponse(exchange, 200, "{\"success\": true}");
                else
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Lỗi thêm bình luận\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }

    public void handleProjectChat(HttpExchange exchange) throws IOException {
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
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Không thể thu hồi tin nhắn\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }

    public void handleMeetings(HttpExchange exchange) throws IOException {
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
                } else
                    sendResponse(exchange, 400, "{\"success\": false}");
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

    public void handleGetNotifications(HttpExchange ex) throws IOException {
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

    public void handleRespondInvite(HttpExchange ex) throws IOException {
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
            } else
                sendResponse(ex, 400, "{\"error\":\"Lỗi xử lý hệ thống!\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleMarkRead(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            int notifId = Integer.parseInt(extractQuery(ex.getRequestURI().getQuery(), "id"));
            new NotificationDAO().markAsRead(notifId);
            sendResponse(ex, 200, "{\"success\": true}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleDeleteNotification(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            int notifId = Integer.parseInt(extractQuery(ex.getRequestURI().getQuery(), "id"));
            if (new NotificationDAO().deleteNotification(notifId)) {
                sendResponse(ex, 200, "{\"success\": true}");
            } else {
                sendResponse(ex, 400, "{\"success\": false}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}