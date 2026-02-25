package com.teamwork.plugins;

import com.teamwork.contract.IHostContext;
import com.teamwork.contract.IPlugin;

public class HelloWorldPlugin implements IPlugin {
    private IHostContext context;

    @Override
    public String getName() {
        return "Hello World Plugin (Phiên bản thử nghiệm)";
    }

    @Override
    public void initialize(IHostContext context) {
        this.context = context;
        this.context.log("[PLUGIN] Bắt đầu thiết lập: " + getName());
    }

    @Override
    public void start() {
        this.context.log("[PLUGIN] Đã chạy thành công! Xin chào toàn đội phát triển.");
        // Ở các giai đoạn sau, Plugin sẽ đăng ký các hàm tính toán, mở API tại đây
    }

    @Override
    public void stop() {
        this.context.log("[PLUGIN] Đang tiến hành dừng và giải phóng tài nguyên...");
    }
}