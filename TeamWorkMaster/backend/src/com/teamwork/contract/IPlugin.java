package com.teamwork.contract;

public interface IPlugin {
    String getName();
    void initialize(IHostContext context);
    void start();
    void stop();
    void onProjectDelete(int projectId);
}