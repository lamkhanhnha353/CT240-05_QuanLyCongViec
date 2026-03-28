package com.teamwork.patterns;

public interface TaskObserver {
    // Hàm sẽ được gọi khi công việc thay đổi trạng thái
    void updateStatus(int taskId, String oldStatus, String newStatus);
}