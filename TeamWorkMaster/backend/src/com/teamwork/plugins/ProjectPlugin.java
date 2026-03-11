package com.teamwork.plugins;

import com.teamwork.contract.IHostContext;
import com.teamwork.contract.IPlugin;
import com.teamwork.db.ProjectDAO;
import com.teamwork.db.ProjectMemberDAO;

public class ProjectPlugin implements IPlugin {

    private IHostContext context;
    private ProjectDAO projectDAO;
    private ProjectMemberDAO memberDAO;

    @Override
    public String getName() {
        return "Project Management Plugin";
    }

    @Override
    public void initialize(IHostContext context) {

        this.context = context;
        this.projectDAO = new ProjectDAO();
        this.memberDAO = new ProjectMemberDAO();

        context.log("[PLUGIN] Khoi tao Project Management Plugin");
    }

    @Override
public void start() {

    context.log("[PROJECT] Plugin quan ly project da bat dau");

    boolean created = projectDAO.createProject(
            "Demo Project",
            "Project demo plugin",
            1
    );

    if (created) {

        context.log("[PROJECT] Tao project thanh cong");

        boolean invited = memberDAO.inviteMember(
                1,
                1,
                "OWNER"
        );

        if (invited) {
            context.log("[PROJECT] Da them thanh vien vao project");
        }
    }

    projectDAO.listProjects();
}

    @Override
    public void stop() {

        context.log("[PLUGIN] Stop Project Plugin");

    }
}