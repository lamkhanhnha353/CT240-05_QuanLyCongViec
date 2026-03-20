package com.teamwork.plugins;

import com.teamwork.contract.IHostContext;
import com.teamwork.contract.IPlugin;

import javax.swing.SwingWorker;
import java.util.List;

public class StatisticsPlugin implements IPlugin {
    private IHostContext context;

    @Override
    public String getName() {
        return "Plugin Thống Kê (Dữ liệu giả)";
    }

    @Override
    public void initialize(IHostContext context) {
        this.context = context;
        this.context.log("[STATISTICS] Khởi tạo Plugin Thống kê thành công.");
    }

    @Override
    public void start() {
        this.context.log("[STATISTICS] Bắt đầu chạy tiến trình thu thập số liệu ngầm...");
        executeStatisticsWorker();
    }

    @Override
    public void stop() {
        this.context.log("[STATISTICS] Đã dừng Plugin Thống kê.");
    }

    private void executeStatisticsWorker() {
        SwingWorker<String, Integer> worker = new SwingWorker<String, Integer>() {

            @Override
            protected String doInBackground() throws Exception {
                context.log("[WORKER] Thread phụ đang làm việc...");
                for (int progress = 10; progress <= 100; progress += 30) {
                    Thread.sleep(800);
                    publish(progress);
                }
                return "{ \"labels\": [\"To Do\", \"In Progress\", \"Done\"], \"data\": [12, 5, 20] }";
            }

            @Override
            protected void process(List<Integer> chunks) {
                int currentProgress = chunks.get(chunks.size() - 1);
                context.log("[WORKER] Đang tính toán dữ liệu... Hoàn thành " + currentProgress + "%");
            }

            @Override
            protected void done() {
                try {
                    String finalJsonResult = get();
                    context.log("[STATISTICS] Phân tích hoàn tất! Dữ liệu xuất ra: " + finalJsonResult);
                    context.log("[STATISTICS] Sẵn sàng gửi dữ liệu này cho Vue.js vẽ biểu đồ.");
                } catch (Exception e) {
                    context.log("[STATISTICS] Lỗi trong quá trình thống kê: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    @Override
    public void onProjectDelete(int projectId) {
        if (this.context != null) {
            this.context.log("[STATISTICS] Received delete signal for project: " + projectId);
        }
    }
}