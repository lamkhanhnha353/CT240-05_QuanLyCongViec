package com.teamwork;

import com.teamwork.core.Task;
import com.teamwork.kernel.DeadlineMonitor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("🚀 Hệ thống thông báo công việc đang chạy...\n");

        List<Task> tasks = new ArrayList<>();

        Task task1 = new Task(
                "Hoàn thành báo cáo Java",
                "Đang làm",
                "nguyenhuynhtuanvi555@gmail.com",
                LocalDateTime.now().plusDays(3));

        tasks.add(task1);

        Thread monitorThread = new Thread(
                new DeadlineMonitor(tasks),
                "DeadlineMonitorThread");

        monitorThread.start();

        System.out.println("📡 Deadline monitor đang hoạt động...");
    }
}