package com.teamwork.core;

import com.teamwork.contract.IHostContext;
import com.teamwork.contract.IPlugin;
import com.teamwork.kernel.PluginLoader;
import com.teamwork.db.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

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
        JMenuItem itemTestDB = new JMenuItem("Test Kết nối MySQL"); // Nút test Database
        JMenuItem itemExit = new JMenuItem("Thoát Server");
        JMenuItem itemStartApi = new JMenuItem("Bật API Server (Cho phép Web kết nối)");

        menuSystem.add(itemLoadPlugin);
        menuSystem.add(itemLoadStats);
        menuSystem.addSeparator();
        menuSystem.add(itemTestDB); // Thêm nút test Database vào Menu
        menuSystem.add(itemStartApi); // Thêm dòng này vào dưới nút itemTestDB
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

        // Sự kiện 1: Nạp Hello World Plugin
        itemLoadPlugin.addActionListener((ActionEvent e) -> {
            log("-> Đang tiến hành quét và nạp plugin bằng Reflection...");

            // Nạp class động
            pluginLoader.loadPluginClass("com.teamwork.plugins.HelloWorldPlugin");

            // Khởi chạy các plugin vừa nạp
            for (IPlugin plugin : pluginLoader.getLoadedPlugins()) {
                try {
                    plugin.initialize(this); // DashboardFrame đóng vai trò là IHostContext
                    plugin.start();
                } catch (Exception ex) {
                    GlobalExceptionHandler.handle("Chạy Plugin " + plugin.getName(), ex);
                }
            }
        });

        // Sự kiện 2: Nạp Statistics Plugin (Test Đa luồng)
        itemLoadStats.addActionListener((ActionEvent e) -> {
            log("-> Đang yêu cầu nạp Plugin Thống kê...");
            pluginLoader.loadPluginClass("com.teamwork.plugins.StatisticsPlugin");

            // Lấy plugin vừa nạp (giả định là phần tử cuối cùng trong list để không chạy
            // lại plugin cũ)
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

        // Sự kiện 3: Test kết nối Database (Singleton)
        itemTestDB.addActionListener((ActionEvent e) -> {
            log("-> Đang thử kết nối tới Cơ sở dữ liệu MySQL...");
            // Lần gọi getInstance() đầu tiên sẽ kích hoạt việc mở kết nối tới DB
            DatabaseConnection.getInstance();
            log("[HỆ THỐNG] Lệnh gọi DatabaseConnection đã thực thi xong.");
        });

        
        // Sự kiện: Bật API Server
        itemStartApi.addActionListener(e -> {
            try {
                log("-> Đang khởi động API Server ở cổng 8080...");
                ApiServer apiServer = new ApiServer(8080);
                apiServer.start();
                log("[HỆ THỐNG] API Server đã bật. Vue.js có thể kết nối ngay bây giờ!");
                itemStartApi.setEnabled(false); // Bật rồi thì khóa nút lại để tránh bật 2 lần
            } catch (IOException ex) {
                log("[LỖI] Không thể bật API Server: " + ex.getMessage());
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