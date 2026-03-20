package com.teamwork.plugins;

import com.teamwork.contract.IHostContext;
import com.teamwork.contract.IPlugin;

public class ProjectPlugin implements IPlugin {
    private IHostContext context;

    @Override
    public String getName() {
        return "Project Management Plugin";
    }

    @Override
    public void initialize(IHostContext context) {
        this.context = context;
    }

    @Override
    public void start() {
        context.log("Project Plugin started.");
    }

    @Override
    public void stop() {
        context.log("Project Plugin stopped.");
    }

    @Override
    public void onProjectDelete(int projectId) {
        context.log("Cleaning up data for Project ID: " + projectId);
    }
}