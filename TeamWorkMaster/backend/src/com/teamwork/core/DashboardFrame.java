package com.teamwork.core;

import com.teamwork.contract.IHostContext;
import com.teamwork.contract.IPlugin;
import com.teamwork.kernel.PluginLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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
        JMenuItem itemExit = new JMenuItem("Thoát Server");

        menuSystem.add(itemLoadPlugin);
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