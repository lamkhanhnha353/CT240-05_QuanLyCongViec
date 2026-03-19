package com.teamwork.service;

import com.teamwork.db.ProjectMemberDAO;

public class PermissionService {

    // Kiểm tra OWNER (bao gồm cả trường hợp OWNER chưa được thêm vào bảng
    // TBL_PROJECT_MEMBERS)
    public boolean isOwner(int userId, int projectId) {
        try {
            Integer ownerId = new com.teamwork.db.ProjectDAO().getOwnerId(projectId);
            if (ownerId != null && ownerId == userId) {
                return true;
            }
        } catch (Exception ignored) {
        }

        ProjectMemberDAO dao = new ProjectMemberDAO();
        String role = dao.getUserRole(userId, projectId);

        return role != null && role.equals("OWNER");
    }

    // Kiểm tra MANAGER (bao gồm OWNER)
    public boolean isManager(int userId, int projectId) {
        if (isOwner(userId, projectId))
            return true;

        ProjectMemberDAO dao = new ProjectMemberDAO();
        String role = dao.getUserRole(userId, projectId);

        return role != null && role.equals("MANAGER");
    }

    // Kiểm tra MEMBER (có tham gia project)
    public boolean isMember(int userId, int projectId) {
        ProjectMemberDAO dao = new ProjectMemberDAO();
        String role = dao.getUserRole(userId, projectId);

        return role != null;
    }
}