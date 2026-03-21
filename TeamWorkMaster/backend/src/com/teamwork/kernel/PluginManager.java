package com.teamwork.kernel;

import com.teamwork.contract.IPlugin;
import java.util.ArrayList;
import java.util.List;

public class PluginManager {
    private static List<IPlugin> plugins = new ArrayList<>();

    public static void addPlugin(IPlugin plugin) {
        plugins.add(plugin);
    }

    public static void notifyProjectDeleted(int projectId) {
        for (IPlugin plugin : plugins) {
            try {
                plugin.onProjectDelete(projectId);
            } catch (Exception e) {
                System.err.println("Error notifying plugin: " + plugin.getName());
            }
        }
    }
}