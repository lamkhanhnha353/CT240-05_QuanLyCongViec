package com.teamwork.kernel;

import com.teamwork.contract.IPlugin;
import com.teamwork.core.GlobalExceptionHandler;
import java.util.ArrayList;
import java.util.List;

public class PluginLoader {
    private List<IPlugin> loadedPlugins = new ArrayList<>();

    /**
     * Nạp một lớp Plugin vào bộ nhớ thông qua tên đầy đủ của lớp (Reflection).
     */
    public void loadPluginClass(String className) {
        try {
            // Sử dụng Reflection để lấy đối tượng Class
            Class<?> clazz = Class.forName(className);

            // Kiểm tra xem lớp này có implement IPlugin hay không
            if (IPlugin.class.isAssignableFrom(clazz)) {
                // Khởi tạo đối tượng từ lớp
                IPlugin plugin = (IPlugin) clazz.getDeclaredConstructor().newInstance();
                loadedPlugins.add(plugin);
            } else {
                throw new Exception("Lớp [" + className + "] không hợp lệ vì không implement IPlugin.");
            }
        } catch (Exception e) {
            // Ném lỗi về cơ chế xử lý lỗi tập trung
            GlobalExceptionHandler.handle("PluginLoader", e);
        }
    }

    public List<IPlugin> getLoadedPlugins() {
        return loadedPlugins;
    }
}