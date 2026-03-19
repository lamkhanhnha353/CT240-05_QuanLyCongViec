package com.teamwork.service;

import com.teamwork.db.ProjectMemberDAO;

public class PermissionService {

    // Kiểm tra OWNER
    public boolean isOwner(int userId, int projectId) {
        ProjectMemberDAO dao = new ProjectMemberDAO();
        String role = dao.getUserRole(userId, projectId);

        return role != null && role.equals("OWNER");
    }

    // Kiểm tra MANAGER (bao gồm OWNER)
    public boolean isManager(int userId, int projectId) {
        ProjectMemberDAO dao = new ProjectMemberDAO();
        String role = dao.getUserRole(userId, projectId);

        return role != null && (role.equals("OWNER") || role.equals("MANAGER"));
    }

    // Kiểm tra MEMBER (có tham gia project)
    public boolean isMember(int userId, int projectId) {
        ProjectMemberDAO dao = new ProjectMemberDAO();
        String role = dao.getUserRole(userId, projectId);

        return role != null;
    }
}