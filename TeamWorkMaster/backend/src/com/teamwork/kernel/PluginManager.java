package com.teamwork.kernel;

import com.teamwork.contract.IPlugin;
import com.teamwork.contract.IHostContext;
import java.util.List;

/**
 * Quản lý vòng đời các Plugin trong hệ thống.
 * Chịu trách nhiệm khởi tạo, chạy và dừng Plugin.
 */
public class PluginManager {

    private List<IPlugin> plugins;

    /**
     * Thiết lập danh sách Plugin đã được nạp.
     */
    public void setPlugins(List<IPlugin> plugins) {
        this.plugins = plugins;
    }

    /**
     * Khởi tạo các Plugin với HostContext.
     */
    public void initializePlugins(IHostContext context) {
        for (IPlugin plugin : plugins) {
            plugin.initialize(context);
        }
    }

    /**
     * Bắt đầu tất cả Plugin.
     */
    public void startAll() {
        for (IPlugin plugin : plugins) {
            plugin.start();
        }
    }

    /**
     * Dừng tất cả Plugin.
     */
    public void stopAll() {
        for (IPlugin plugin : plugins) {
            plugin.stop();
        }
    }
}