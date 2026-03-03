package com.teamwork.kernel;

import com.teamwork.contract.IPlugin;
import com.teamwork.core.GlobalExceptionHandler;
import java.util.ArrayList;
import java.util.List;

public class PluginLoader {
    private List<IPlugin> loadedPlugins = new ArrayList<>();

    /**
     * Nạp một lớp Plugin vào bộ nhớ thông qua tên đầy đủ của lớp.
     */
    public void loadPluginClass(String className) {
        try {
            // Sử dụng Reflection để lấy đối tượng Class
            Class<?> clazz = Class.forName(className);

            // Kiểm tra xem lớp này có implement IPlugin hay không, kiểm tra tên nạp vàocó hợp lệ không
            // Nếu một file class lạ hoặc không đạt chuẩn bị nạp vào, lệnh isAssignableFrom
            // sẽ trả về false, hệ thống từ chối nạp để bảo vệ an toàn cho Core.
            if (IPlugin.class.isAssignableFrom(clazz)) {
                // Khởi tạo đối tượng từ lớp
                IPlugin plugin = (IPlugin) clazz.getDeclaredConstructor().newInstance();
            // Khởi tạo đối tượng: Sau khi xác nhận class đã tuân thủ đúng hợp đồng, dòng
            // này dùng Reflection để gọi hàm khởi tạo (Constructor) mặc định, tạo ra một
            // thực thể (instance) sống của Plugin đó, ép kiểu về IPlugin và thêm vào danh
            // sách loadedPlugins.
                loadedPlugins.add(plugin);
            } else {
        // nếu Plugin bị lỗi (ví dụ: gõ sai tên class, class bị hỏng), lỗi này sẽ rơi vào block catch
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