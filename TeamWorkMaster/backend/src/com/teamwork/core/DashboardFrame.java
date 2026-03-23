package com.teamwork.core;

import com.teamwork.contract.IHostContext;
import com.teamwork.contract.IPlugin;
import com.teamwork.db.DatabaseConnection;
import com.teamwork.kernel.PluginLoader;
// 🟢 IMPORT THƯ VIỆN BOT Ở ĐÂY 🟢
import com.teamwork.plugins.DeadlineBot;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * Container chính của hệ thống (Đóng vai trò là Bảng điều khiển Server).
 */
public class DashboardFrame extends JFrame implements IHostContext {
    private JTextArea logArea;
    private PluginLoader pluginLoader;

    public DashboardFrame() {
        pluginLoader = new PluginLoader();
        setupUI();
        log("=== TEAMWORK MASTER CORE SERVER ===");
        log("Hệ thống khởi động thành công. Trạng thái: Sẵn sàng nạp Plugin.");

        // 🟢 ĐÁNH THỨC CON BOT ĐÒI NỢ NGAY KHI SERVER BẬT 🟢
        try {
            log("-> Đang khởi động Deadline Bot (Tiến trình chạy ngầm)...");
            DeadlineBot.startBot();
            log("[HỆ THỐNG] Deadline Bot đã thức dậy! Tự động rà soát công việc trễ hạn.");
        } catch (Exception ex) {
            log("[LỖI] Không thể khởi động Deadline Bot: " + ex.getMessage());
        }
    }

    private void setupUI() {
        setTitle("TeamWork Master - Server Dashboard");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Giữa màn hình
        setLayout(new BorderLayout());

        // 1. Hệ thống Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menuSystem = new JMenu("Hệ thống Quản Trị");

        JMenuItem itemLoadPlugin = new JMenuItem("Nạp Hello World Plugin");
        JMenuItem itemLoadStats = new JMenuItem("Chạy Plugin Thống Kê (Test Đa Luồng)");
        JMenuItem itemTestDB = new JMenuItem("Test Kết nối MySQL");
        JMenuItem itemStartApi = new JMenuItem("Bật API Server (Cho phép Web kết nối)");
        JMenuItem itemLoadProject = new JMenuItem("Nạp Plugin Quản Lý Project");
        JMenuItem itemExit = new JMenuItem("Thoát Server");

        menuSystem.add(itemLoadPlugin);
        menuSystem.add(itemLoadStats);
        menuSystem.addSeparator();
        menuSystem.add(itemTestDB);
        menuSystem.add(itemStartApi);
        menuSystem.addSeparator();
        menuSystem.add(itemLoadProject);
        menuSystem.addSeparator();
        menuSystem.add(itemExit);

        menuBar.add(menuSystem);
        setJMenuBar(menuBar);

        // 2. Vùng Layout hiển thị Log (Console mô phỏng)
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // 3. Xử lý sự kiện Menu
        itemExit.addActionListener(e -> System.exit(0));

        // Sự kiện: Nạp Hello World Plugin
        itemLoadPlugin.addActionListener((ActionEvent e) -> {
            log("-> Đang tiến hành quét và nạp plugin bằng Reflection...");
            pluginLoader.loadPluginClass("com.teamwork.plugins.HelloWorldPlugin");
            for (IPlugin plugin : pluginLoader.getLoadedPlugins()) {
                try {
                    plugin.initialize(this); // DashboardFrame đóng vai trò là IHostContext
                    plugin.start();
                } catch (Exception ex) {
                    GlobalExceptionHandler.handle("Chạy Plugin " + plugin.getName(), ex);
                }
            }
        });

        // Sự kiện: Nạp Statistics Plugin (Của đồng đội)
        itemLoadStats.addActionListener((ActionEvent e) -> {
            log("-> Đang yêu cầu nạp Plugin Thống kê...");
            pluginLoader.loadPluginClass("com.teamwork.plugins.StatisticsPlugin");
            int lastIndex = pluginLoader.getLoadedPlugins().size() - 1;
            if (lastIndex >= 0) {
                IPlugin statsPlugin = pluginLoader.getLoadedPlugins().get(lastIndex);
                try {
                    statsPlugin.initialize(this);
                    statsPlugin.start();
                } catch (Exception ex) {
                    GlobalExceptionHandler.handle("Chạy Plugin Thống Kê", ex);
                }
            }
        });

        // Sự kiện: Test kết nối Database
        itemTestDB.addActionListener((ActionEvent e) -> {
            log("-> Đang thử kết nối tới Cơ sở dữ liệu MySQL...");
            DatabaseConnection.getInstance();
            log("[HỆ THỐNG] Lệnh gọi DatabaseConnection đã thực thi xong.");
        });

        // Sự kiện: Bật API Server
        itemStartApi.addActionListener(e -> {
            try {
                log("-> Đang khởi động API Server ở cổng 8080...");
                ApiServer apiServer = new ApiServer(8080);
                apiServer.start();
                log("[HỆ THỐNG] API Server đã bật. Web Vue.js có thể kết nối!");
                itemStartApi.setEnabled(false); // Khóa nút sau khi bật thành công
            } catch (Exception ex) {
                log("[LỖI] Không thể bật API Server: " + ex.getMessage());
            }
        });

        // Sự kiện: Nạp Plugin Quản Lý Project (Của bạn)
        itemLoadProject.addActionListener((ActionEvent e) -> {
            log("-> Đang nạp Project Plugin...");
            pluginLoader.loadPluginClass("com.teamwork.plugins.ProjectPlugin");
            int lastIndex = pluginLoader.getLoadedPlugins().size() - 1;
            if (lastIndex >= 0) {
                IPlugin projectPlugin = pluginLoader.getLoadedPlugins().get(lastIndex);
                try {
                    projectPlugin.initialize(this);
                    projectPlugin.start();
                } catch (Exception ex) {
                    GlobalExceptionHandler.handle("Chạy Project Plugin", ex);
                }
            }
        });
    }

    @Override
    public void log(String message) {
        logArea.append("> " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        // Áp dụng Look & Feel của hệ điều hành
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            new DashboardFrame().setVisible(true);
        });
    }
}