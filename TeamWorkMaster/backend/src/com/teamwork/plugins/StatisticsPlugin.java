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
        // Gọi hàm thực thi đa luồng
        executeStatisticsWorker();
    }

    @Override
    public void stop() {
        this.context.log("[STATISTICS] Đã dừng Plugin Thống kê.");
    }

    /**
     * Hàm áp dụng SwingWorker để xử lý đa luồng.
     * SwingWorker<Kết_quả_trả_về_cuối_cùng, Kiểu_dữ_liệu_cập_nhật_tiến_độ>
     */
    private void executeStatisticsWorker() {
        SwingWorker<String, Integer> worker = new SwingWorker<String, Integer>() {

            // 1. doInBackground(): Chạy trên một luồng nền (Background Thread) độc lập
            // Tuyệt đối KHÔNG thao tác giao diện ở đây để tránh làm đơ ứng dụng
            @Override
            protected String doInBackground() throws Exception {
                context.log("[WORKER] Thread phụ đang làm việc...");

                // Giả lập quá trình đọc hàng ngàn bản ghi từ Database mất thời gian
                for (int progress = 10; progress <= 100; progress += 30) {
                    Thread.sleep(800); // Tạm dừng 0.8 giây để giả lập độ trễ truy vấn
                    publish(progress); // Gửi tiến độ hiện tại ra ngoài cho hàm process()
                }

                // Trả về dữ liệu JSON giả (Mock Data) sau khi tính toán xong
                return "{ \"labels\": [\"To Do\", \"In Progress\", \"Done\"], \"data\": [12, 5, 20] }";
            }

            // 2. process(): Chạy trên luồng giao diện chính (EDT)
            // Nhận dữ liệu từ publish() để cập nhật tiến độ lên màn hình
            @Override
            protected void process(List<Integer> chunks) {
                int currentProgress = chunks.get(chunks.size() - 1);
                context.log("[WORKER] Đang tính toán dữ liệu... Hoàn thành " + currentProgress + "%");
            }

            // 3. done(): Chạy trên luồng giao diện chính khi doInBackground() kết thúc
            @Override
            protected void done() {
                try {
                    String finalJsonResult = get(); // Lấy kết quả từ doInBackground
                    context.log("[STATISTICS] Phân tích hoàn tất! Dữ liệu xuất ra: " + finalJsonResult);
                    context.log("[STATISTICS] Sẵn sàng gửi dữ liệu này cho Vue.js vẽ biểu đồ.");
                } catch (Exception e) {
                    context.log("[STATISTICS] Lỗi trong quá trình thống kê: " + e.getMessage());
                }
            }
        };

        // Kích hoạt luồng chạy
        worker.execute();
    }
}