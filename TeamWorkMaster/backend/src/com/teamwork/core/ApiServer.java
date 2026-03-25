package com.teamwork.core;

import com.sun.net.httpserver.HttpServer;
import com.teamwork.handlers.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class ApiServer {
    private HttpServer server;

    public ApiServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Khởi tạo các "Ban bệ" xử lý
        AuthHandler auth = new AuthHandler();
        UserHandler user = new UserHandler();
        ProjectHandler project = new ProjectHandler();
        TaskHandler task = new TaskHandler();
        AdminHandler admin = new AdminHandler();
        CommunicationHandler comm = new CommunicationHandler();
        DashboardHandler dash = new DashboardHandler();

        // 1. Nhóm Xác thực
        server.createContext("/api/login", auth::handleLogin);
        server.createContext("/api/register", auth::handleRegister);
        server.createContext("/api/forgot-password", auth::handleForgotPassword);

        // 2. Nhóm Người dùng
        server.createContext("/api/users/search", user::handleSearchUsers);
        server.createContext("/api/users/profile", user::handleUserProfile);
        server.createContext("/api/users/change-password", user::handleChangePassword);

        // 3. Nhóm Dự án
        server.createContext("/api/projects/list", project::handleList);
        server.createContext("/api/projects/create", project::handleCreate);
        server.createContext("/api/projects/update", project::handleUpdate);
        server.createContext("/api/projects/delete", project::handleDelete);
        server.createContext("/api/projects/add-member", project::handleAddMember);
        server.createContext("/api/projects/members-list", project::handleProjectMembersList);
        server.createContext("/api/projects/update-role", project::handleUpdateMemberRole);
        server.createContext("/api/projects/remove-member", project::handleRemoveMember);

        // 4. Nhóm Công việc (Task)
        server.createContext("/api/tasks/list", task::handleTaskList);
        server.createContext("/api/tasks/my-tasks", task::handleMyTasks);
        server.createContext("/api/tasks/create", task::handleTaskCreate);
        server.createContext("/api/tasks/delete", task::handleTaskDelete);
        server.createContext("/api/tasks/update-details", task::handleTaskUpdateDetails);
        server.createContext("/api/tasks/update-status", task::handleTaskUpdateStatus);
        server.createContext("/api/tasks/update-order", task::handleTaskUpdateOrder);
        server.createContext("/api/tasks/subtasks", task::handleSubtasks);
        server.createContext("/api/tasks/logs", task::handleTaskLogs);
        server.createContext("/api/tasks/attachments", task::handleTaskAttachments);

        // 5. Nhóm Giao tiếp & Thông báo
        server.createContext("/api/comments", comm::handleComments);
        server.createContext("/api/project-chat", comm::handleProjectChat);
        server.createContext("/api/meetings", comm::handleMeetings);
        server.createContext("/api/notifications/list", comm::handleGetNotifications);
        server.createContext("/api/notifications/respond", comm::handleRespondInvite);
        server.createContext("/api/notifications/read", comm::handleMarkRead);
        server.createContext("/api/notifications/delete", comm::handleDeleteNotification);

        // 6. Nhóm Báo cáo & Thống kê
        server.createContext("/api/dashboard/summary", dash::handleDashboardSummary);
        server.createContext("/api/statistics", dash::handleStatistics);

        // 7. Nhóm Quản trị viên (Admin)
        server.createContext("/api/admin/users", admin::handleGetUsers);
        server.createContext("/api/admin/users/create", admin::handleCreateUser);
        server.createContext("/api/admin/users/update", admin::handleUpdateUser);
        server.createContext("/api/admin/users/delete", admin::handleDeleteUser);
        server.createContext("/api/admin/users/toggle-lock", admin::handleToggleLockUser);
        server.createContext("/api/admin/dashboard/summary", admin::handleDashboardSummary);

        server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println(">>> BACKEND ĐANG CHẠY TẠI CỔNG " + server.getAddress().getPort());
        System.out.println(">>> TRẠNG THÁI: API SERVER (CLEAN ARCHITECTURE) SẴN SÀNG!");
    }
}