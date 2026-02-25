package com.teamwork.contract;

/**
 * Interface đại diện cho Core Framework (Container).
 * Cung cấp các dịch vụ dùng chung cho Plugin (ví dụ: ghi log, kết nối DB sau
 * này).
 */
public interface IHostContext {
    void log(String message);
}