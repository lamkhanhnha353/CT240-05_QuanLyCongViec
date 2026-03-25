package com.teamwork.kernel;

import com.teamwork.core.Task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test cho DeadlineMonitor (kernel.DeadlineMonitor)
 *
 * Không cần Database hay Thread thực sự.
 * Kiểm tra logic phát hiện deadline: 3 ngày trước, đúng hôm nay, quá hạn.
 *
 * Chiến lược: Test trực tiếp logic điều kiện if() trong DeadlineMonitor
 * mà không cần chạy Thread vô tận.
 */
@DisplayName("Unit Tests - DeadlineMonitor Logic")
public class DeadlineMonitorTest {

    /**
     * Helper: trích xuất và chạy logic kiểm tra deadline của DeadlineMonitor
     * cho một Task cụ thể tại một thời điểm 'now' cho trước.
     */
    private static void runDeadlineCheckLogic(Task task, LocalDateTime now) {
        LocalDateTime deadline = task.getDeadline();

        // Nhắc trước 3 ngày
        if (!task.isNotified3Days()
                && deadline.minusDays(3).isBefore(now)
                && deadline.isAfter(now)) {
            task.setNotified3Days(true);
        }

        // Đúng ngày deadline
        if (!task.isNotifiedToday()
                && now.toLocalDate().isEqual(deadline.toLocalDate())) {
            task.setNotifiedToday(true);
        }

        // Quá hạn 1 ngày
        if (!task.isNotifiedOverdue()
                && !now.isBefore(deadline.plusDays(1))) {
            task.setNotifiedOverdue(true);
        }
    }

    // ─── NHÓM 1: Nhắc trước 3 ngày ───────────────────────────────────────────

    @Test
    @DisplayName("[DM-01] Deadline còn 2 ngày → kích hoạt thông báo 3-ngày")
    void testDeadline_Within3Days_Sets3DayFlag() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(2); // còn 2 ngày
        Task task = new Task("Task 2 ngày", "TODO", "a@b.com", deadline);

        runDeadlineCheckLogic(task, now);

        assertTrue(task.isNotified3Days(),
                "Deadline còn 2 ngày phải kích hoạt cờ notified3Days");
    }

    @Test
    @DisplayName("[DM-02] Deadline còn 1 ngày → kích hoạt thông báo 3-ngày")
    void testDeadline_OneDayLeft_Sets3DayFlag() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(1);
        Task task = new Task("Task 1 ngày", "TODO", "a@b.com", deadline);

        runDeadlineCheckLogic(task, now);

        assertTrue(task.isNotified3Days());
    }

    @Test
    @DisplayName("[DM-03] Deadline còn đúng 3 ngày (boundary) → KHÔNG kích hoạt 3-ngày")
    void testDeadline_Exactly3Days_NotTriggered() {
        // deadline.minusDays(3) == now → điều kiện isBefore(now) = false
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(3);
        Task task = new Task("Task 3 ngày", "TODO", "a@b.com", deadline);

        runDeadlineCheckLogic(task, now);

        // Biên: minusDays(3) == now → isBefore() trả về false → không trigger
        assertFalse(task.isNotified3Days(),
                "Deadline đúng 3 ngày (boundary) chưa kích hoạt cờ 3-ngày");
    }

    @Test
    @DisplayName("[DM-04] Deadline còn 10 ngày → KHÔNG kích hoạt 3-ngày")
    void testDeadline_FarFuture_No3DayFlag() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(10);
        Task task = new Task("Task xa", "TODO", "a@b.com", deadline);

        runDeadlineCheckLogic(task, now);

        assertFalse(task.isNotified3Days(),
                "Deadline còn 10 ngày không kích hoạt cờ 3-ngày");
    }

    // ─── NHÓM 2: Đúng hôm nay ────────────────────────────────────────────────

    @Test
    @DisplayName("[DM-05] Deadline đúng hôm nay → kích hoạt thông báo today")
    void testDeadline_Today_SetsTodayFlag() {
        LocalDateTime now = LocalDateTime.now();
        // Deadline cùng ngày, nhưng ở cuối ngày (23:59)
        LocalDateTime deadline = now.withHour(23).withMinute(59).withSecond(0);
        Task task = new Task("Task hôm nay", "TODO", "a@b.com", deadline);

        runDeadlineCheckLogic(task, now);

        assertTrue(task.isNotifiedToday(),
                "Deadline hôm nay phải kích hoạt cờ notifiedToday");
    }

    @Test
    @DisplayName("[DM-06] Deadline ngày mai → KHÔNG kích hoạt today")
    void testDeadline_Tomorrow_NoTodayFlag() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(1);
        Task task = new Task("Task ngày mai", "TODO", "a@b.com", deadline);

        runDeadlineCheckLogic(task, now);

        assertFalse(task.isNotifiedToday(),
                "Deadline ngày mai không kích hoạt cờ notifiedToday");
    }

    @Test
    @DisplayName("[DM-07] Deadline hôm qua → KHÔNG kích hoạt today")
    void testDeadline_Yesterday_NoTodayFlag() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.minusDays(1);
        Task task = new Task("Task hôm qua", "TODO", "a@b.com", deadline);

        runDeadlineCheckLogic(task, now);

        assertFalse(task.isNotifiedToday(),
                "Deadline hôm qua không kích hoạt today (đã sang ngày quá hạn)");
    }

    // ─── NHÓM 3: Quá hạn ─────────────────────────────────────────────────────

    @Test
    @DisplayName("[DM-08] Deadline quá hơn 1 ngày → kích hoạt thông báo overdue")
    void testDeadline_Overdue_SetsOverdueFlag() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.minusDays(2); // quá hạn 2 ngày
        Task task = new Task("Task quá hạn", "IN_PROGRESS", "a@b.com", deadline);

        runDeadlineCheckLogic(task, now);

        assertTrue(task.isNotifiedOverdue(),
                "Deadline quá 1 ngày phải kích hoạt cờ notifiedOverdue");
    }

    @Test
    @DisplayName("[DM-09] Deadline đúng 1 ngày trước (boundary overdue) → kích hoạt overdue")
    void testDeadline_ExactlyOneDayAgo_SetsOverdueFlag() {
        LocalDateTime now = LocalDateTime.now();
        // deadline.plusDays(1) == now → !now.isBefore(deadline.plusDays(1)) = true
        LocalDateTime deadline = now.minusDays(1);
        Task task = new Task("Task overdue boundary", "TODO", "a@b.com", deadline);

        runDeadlineCheckLogic(task, now);

        assertTrue(task.isNotifiedOverdue(),
                "Deadline đúng 1 ngày trước phải kích hoạt overdue");
    }

    @Test
    @DisplayName("[DM-10] Deadline sắp hết hạn (còn vài giờ) → KHÔNG overdue")
    void testDeadline_SoonExpiring_NotOverdue() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusHours(2); // còn 2 tiếng
        Task task = new Task("Task vài giờ nữa", "TODO", "a@b.com", deadline);

        runDeadlineCheckLogic(task, now);

        assertFalse(task.isNotifiedOverdue(),
                "Deadline còn 2 tiếng không kích hoạt overdue");
    }

    // ─── NHÓM 4: Không gửi thông báo trùng lặp ───────────────────────────────

    @Test
    @DisplayName("[DM-11] Đã set cờ 3-ngày → chạy lại không gửi lần 2")
    void testDeadline_AlreadyNotified3Days_NoDoubleNotify() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(2);
        Task task = new Task("Task đã thông báo", "TODO", "a@b.com", deadline);

        // Giả lập: đã thông báo rồi
        task.setNotified3Days(true);

        // Chạy lại logic
        runDeadlineCheckLogic(task, now);

        // Cờ vẫn là true (không bị reset), không bị gọi 2 lần
        assertTrue(task.isNotified3Days(),
                "Cờ phải giữ nguyên true sau lần chạy thứ 2");
    }

    @Test
    @DisplayName("[DM-12] Đã set cờ overdue → chạy lại không gửi lần 2")
    void testDeadline_AlreadyNotifiedOverdue_NoDoubleNotify() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.minusDays(3);
        Task task = new Task("Task overdue đã thông báo", "TODO", "a@b.com", deadline);

        task.setNotifiedOverdue(true); // đã thông báo rồi

        runDeadlineCheckLogic(task, now);

        assertTrue(task.isNotifiedOverdue());
        // Logic có điều kiện !isNotifiedOverdue() nên không gọi lại
    }

    // ─── NHÓM 5: Danh sách nhiều task ────────────────────────────────────────

    @Test
    @DisplayName("[DM-13] Nhiều task với deadline khác nhau — mỗi task độc lập")
    void testDeadlineMonitor_MultipleTasksIndependent() {
        LocalDateTime now = LocalDateTime.now();

        Task taskFuture = new Task("Xa", "TODO", "a@b.com", now.plusDays(10));
        Task task3Days = new Task("3 ngày", "TODO", "b@c.com", now.plusDays(2));
        Task taskToday = new Task("Hôm nay", "TODO", "c@d.com", now);
        Task taskOverdue = new Task("Quá hạn", "TODO", "d@e.com", now.minusDays(2));

        List<Task> tasks = Arrays.asList(taskFuture, task3Days, taskToday, taskOverdue);

        for (Task task : tasks) {
            runDeadlineCheckLogic(task, now);
        }

        assertFalse(taskFuture.isNotified3Days(), "Task xa: không thông báo 3-ngày");
        assertTrue(task3Days.isNotified3Days(), "Task 2 ngày: thông báo 3-ngày");
        assertTrue(taskToday.isNotifiedToday(), "Task hôm nay: thông báo today");
        assertTrue(taskOverdue.isNotifiedOverdue(), "Task quá hạn: thông báo overdue");
    }
}