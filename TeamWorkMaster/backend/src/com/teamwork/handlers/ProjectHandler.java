package com.teamwork.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.teamwork.db.*;
import com.teamwork.service.PermissionService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ProjectHandler extends BaseHandler {

    public void handleList(HttpExchange ex) throws IOException {
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

    public void handleCreate(HttpExchange ex) throws IOException {
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
            } else
                sendResponse(ex, 400, "{\"error\":\"Tạo thất bại\"}");
        } catch (Exception e) {
            sendResponse(ex, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleUpdate(HttpExchange ex) throws IOException {
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

    public void handleDelete(HttpExchange ex) throws IOException {
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
            } else
                sendResponse(ex, 400, "{\"error\":\"Lỗi CSDL\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleProjectMembersList(HttpExchange ex) throws IOException {
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

    public void handleAddMember(HttpExchange ex) throws IOException {
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

            // DAO đã đảm nhiệm luôn việc lưu DB + Gửi Email (Bức thư số 1)
            String result = new ProjectMemberDAO().inviteMember(projectId, projectName, identifier,
                    extract(body, "role"), inviterName);

            if ("SUCCESS".equals(result)) {
                // Đã xóa luồng gửi Email thừa (Bức thư số 2) ở đây cho khỏi trùng lặp
                sendResponse(ex, 200, "{\"message\":\"Đã gửi lời mời và Email thành công!\"}");
            } else {
                sendResponse(ex, 400, "{\"error\":\"" + result + "\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleUpdateMemberRole(HttpExchange ex) throws IOException {
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

    public void handleRemoveMember(HttpExchange ex) throws IOException {
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
}