package com.teamwork.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.teamwork.db.TaskDAO;
import com.teamwork.db.DatabaseConnection;
import com.teamwork.service.PermissionService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TaskHandler extends BaseHandler {
    private TaskDAO taskDAO = new TaskDAO();

    public void handleTaskDelete(HttpExchange ex) throws IOException {
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

    public void handleTaskUpdateDetails(HttpExchange ex) throws IOException {
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

            if (taskDAO.updateTaskDetails(taskId, title, extract(body, "description"), extract(body, "priority"),
                    newDeadline, extract(body, "startDate"), extract(body, "tags"), assigneeIds)) {
                taskDAO.addTaskLog(taskId, currentUserId, "Đã cập nhật chi tiết công việc");
                String checkNewDl = (newDeadline == null || newDeadline.trim().isEmpty()) ? "null" : newDeadline;
                if (!oldDeadline.equals(checkNewDl)) {
                    final int fProjectId = projectId;
                    final String fProjectName = projectName;
                    final String fOldDl = oldDeadline;
                    final String fNewDl = checkNewDl;
                    final String fUName = uName;
                    new Thread(() -> {
                        com.teamwork.plugins.TaskActionNotifier.notifyDeadlineChanged(fProjectId, fProjectName, title,
                                assigneeIds, fOldDl, fNewDl, fUName);
                    }).start();
                }
                sendResponse(ex, 200, "{\"message\":\"Cập nhật thành công\"}");
            } else
                sendResponse(ex, 400, "{\"error\":\"Lỗi CSDL khi cập nhật\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleTaskList(HttpExchange ex) throws IOException {
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

    public void handleMyTasks(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int userId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;
            if (userId > 0)
                sendResponse(ex, 200, taskDAO.getMyTasks(userId));
            else
                sendResponse(ex, 401, "{\"error\":\"Không xác định được người dùng!\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleTaskCreate(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int projectId = Integer.parseInt(extract(body, "projectId"));
            String title = extract(body, "title");
            String assigneeIds = extract(body, "assigneeIds");
            String priority = extract(body, "priority").isEmpty() ? "MEDIUM" : extract(body, "priority");
            String status = extract(body, "targetColumn").isEmpty() ? "TODO" : extract(body, "targetColumn");

            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int assignerId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 1;

            if (taskDAO.createTask(projectId, title, extract(body, "description"), priority, extract(body, "deadline"),
                    extract(body, "startDate"), extract(body, "tags"), status, assigneeIds)) {
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

    public void handleTaskUpdateStatus(HttpExchange ex) throws IOException {
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

    public void handleTaskUpdateOrder(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String taskIdsStr = extract(body, "taskIds");
            if (!taskIdsStr.isEmpty()) {
                String[] ids = taskIdsStr.split(",");
                for (int i = 0; i < ids.length; i++) {
                    if (!ids[i].trim().isEmpty())
                        taskDAO.updateTaskOrder(Integer.parseInt(ids[i].trim()), i);
                }
            }
            sendResponse(ex, 200, "{\"message\":\"Cập nhật thứ tự thành công\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleSubtasks(HttpExchange exchange) throws IOException {
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
                if (taskDAO.addSubtask(Integer.parseInt(extract(body, "taskId")), extract(body, "title")))
                    sendResponse(exchange, 200, "{\"success\":true}");
                else
                    sendResponse(exchange, 400, "{\"success\":false}");
            } else if ("PUT".equals(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                if (taskDAO.toggleSubtask(Integer.parseInt(extract(body, "subtaskId")),
                        Boolean.parseBoolean(extract(body, "isCompleted"))))
                    sendResponse(exchange, 200, "{\"success\":true}");
                else
                    sendResponse(exchange, 400, "{\"success\":false}");
            } else if ("DELETE".equals(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                if (taskDAO.deleteSubtask(Integer.parseInt(extract(body, "subtaskId"))))
                    sendResponse(exchange, 200, "{\"success\":true}");
                else
                    sendResponse(exchange, 400, "{\"success\":false}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleTaskLogs(HttpExchange exchange) throws IOException {
        handleCors(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 200, taskDAO
                        .getTaskLogs(Integer.parseInt(extractQuery(exchange.getRequestURI().getQuery(), "taskId"))));
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleTaskAttachments(HttpExchange exchange) throws IOException {
        handleCors(exchange);
        String method = exchange.getRequestMethod();
        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        try {
            if ("GET".equals(method)) {
                sendResponse(exchange, 200, taskDAO.getTaskAttachments(
                        Integer.parseInt(extractQuery(exchange.getRequestURI().getQuery(), "taskId"))));
            } else if ("POST".equals(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                int taskId = Integer.parseInt(extract(body, "taskId"));
                String fileName = extract(body, "fileName");
                String userIdStr = exchange.getRequestHeaders().getFirst("User-ID");
                int userId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;

                if (taskDAO.addTaskAttachment(taskId, userId, fileName, extract(body, "fileUrl"))) {
                    taskDAO.addTaskLog(taskId, userId, "Đã đính kèm tài liệu: " + fileName);
                    sendResponse(exchange, 200, "{\"success\":true}");
                } else
                    sendResponse(exchange, 400, "{\"success\":false}");
            } else if ("DELETE".equals(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                if (taskDAO.deleteTaskAttachment(Integer.parseInt(extract(body, "attachmentId"))))
                    sendResponse(exchange, 200, "{\"success\":true}");
                else
                    sendResponse(exchange, 400, "{\"success\":false}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}