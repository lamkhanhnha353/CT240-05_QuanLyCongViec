package com.teamwork.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test cho lớp Task (core.Task)
 *
 * Không cần kết nối Database.
 * Kiểm tra logic nghiệp vụ thuần túy của Task.
 */
@DisplayName("Unit Tests - Task (core.Task)")
public class TaskCoreTest {

    private Task task;
    private LocalDateTime futureDeadline;

    @BeforeEach
    void setUp() {
        // Khởi tạo một Task mẫu trước mỗi test
        futureDeadline = LocalDateTime.now().plusDays(5);
        task = new Task("Viết báo cáo", "TODO", "dev@example.com", futureDeadline);
    }

    // ─── NHÓM 1: Constructor & Getter ────────────────────────────────────────

    @Test
    @DisplayName("[TC-01] Constructor khởi tạo đúng tất cả thuộc tính")
    void testConstructor_AllFieldsInitializedCorrectly() {
        assertAll("Kiểm tra tất cả thuộc tính sau khi tạo Task",
                () -> assertEquals("Viết báo cáo", task.getName(),
                        "getName() phải trả về tên đã truyền vào"),
                () -> assertEquals("TODO", task.getStatus(),
                        "getStatus() phải trả về status ban đầu"),
                () -> assertEquals("dev@example.com", task.getUserEmail(),
                        "getUserEmail() phải trả về email đã truyền vào"),
                () -> assertEquals(futureDeadline, task.getDeadline(),
                        "getDeadline() phải trả về deadline đã truyền vào"));
    }

    @Test
    @DisplayName("[TC-02] Các cờ thông báo mặc định là false")
    void testNotificationFlags_DefaultFalse() {
        assertAll("Tất cả cờ thông báo phải là false khi mới tạo",
                () -> assertFalse(task.isNotified3Days(),
                        "isNotified3Days() phải là false ban đầu"),
                () -> assertFalse(task.isNotifiedToday(),
                        "isNotifiedToday() phải là false ban đầu"),
                () -> assertFalse(task.isNotifiedOverdue(),
                        "isNotifiedOverdue() phải là false ban đầu"));
    }

    // ─── NHÓM 2: setStatus() ─────────────────────────────────────────────────

    @Test
    @DisplayName("[TC-03] setStatus() cập nhật đúng khi đổi sang status mới")
    void testSetStatus_NewStatus_UpdatesCorrectly() {
        task.setStatus("IN_PROGRESS");
        assertEquals("IN_PROGRESS", task.getStatus(),
                "Status phải được cập nhật thành IN_PROGRESS");
    }

    @Test
    @DisplayName("[TC-04] setStatus() không thay đổi nếu gán cùng status cũ")
    void testSetStatus_SameStatus_NoChange() {
        // Task đang là TODO, gán lại TODO → không thay đổi
        task.setStatus("TODO");
        assertEquals("TODO", task.getStatus(),
                "Status không được thay đổi khi gán cùng giá trị cũ");
    }

    @Test
    @DisplayName("[TC-05] setStatus() có thể chuyển từ TODO sang DONE")
    void testSetStatus_TODO_to_DONE() {
        task.setStatus("DONE");
        assertEquals("DONE", task.getStatus());
    }

    @Test
    @DisplayName("[TC-06] setStatus() có thể chuyển từ IN_PROGRESS sang CANCEL")
    void testSetStatus_InProgress_to_Cancel() {
        task.setStatus("IN_PROGRESS");
        task.setStatus("CANCEL");
        assertEquals("CANCEL", task.getStatus());
    }

    // ─── NHÓM 3: Notification flags ──────────────────────────────────────────

    @Test
    @DisplayName("[TC-07] setNotified3Days(true) → isNotified3Days() trả về true")
    void testSetNotified3Days_True() {
        task.setNotified3Days(true);
        assertTrue(task.isNotified3Days(),
                "Sau khi set true, isNotified3Days() phải trả về true");
    }

    @Test
    @DisplayName("[TC-08] setNotifiedToday(true) → isNotifiedToday() trả về true")
    void testSetNotifiedToday_True() {
        task.setNotifiedToday(true);
        assertTrue(task.isNotifiedToday());
    }

    @Test
    @DisplayName("[TC-09] setNotifiedOverdue(true) → isNotifiedOverdue() trả về true")
    void testSetNotifiedOverdue_True() {
        task.setNotifiedOverdue(true);
        assertTrue(task.isNotifiedOverdue());
    }

    @Test
    @DisplayName("[TC-10] Có thể set lại flag từ true về false")
    void testNotificationFlag_SetBackToFalse() {
        task.setNotified3Days(true);
        task.setNotified3Days(false);
        assertFalse(task.isNotified3Days(),
                "Flag phải trở về false sau khi set lại");
    }

    // ─── NHÓM 4: Kiểm tra Deadline ───────────────────────────────────────────

    @Test
    @DisplayName("[TC-11] Task với deadline trong tương lai — hợp lệ")
    void testTask_FutureDeadline_Valid() {
        LocalDateTime future = LocalDateTime.now().plusDays(10);
        Task futureTask = new Task("Task tương lai", "TODO", "a@b.com", future);
        assertTrue(futureTask.getDeadline().isAfter(LocalDateTime.now()),
                "Deadline phải sau thời điểm hiện tại");
    }

    @Test
    @DisplayName("[TC-12] Task với deadline đã qua — phát hiện quá hạn đúng")
    void testTask_PastDeadline_IsOverdue() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        Task overdueTask = new Task("Task quá hạn", "IN_PROGRESS", "b@c.com", past);
        assertTrue(overdueTask.getDeadline().isBefore(LocalDateTime.now()),
                "Deadline trong quá khứ phải nhỏ hơn thời điểm hiện tại");
    }

    @Test
    @DisplayName("[TC-13] Deadline đúng hôm nay — nhận diện chính xác")
    void testTask_TodayDeadline() {
        LocalDateTime todayNoon = LocalDateTime.now().withHour(23).withMinute(59);
        Task todayTask = new Task("Task hôm nay", "TODO", "c@d.com", todayNoon);
        assertTrue(todayTask.getDeadline().toLocalDate()
                .isEqual(LocalDateTime.now().toLocalDate()),
                "Task có deadline hôm nay phải được nhận diện đúng");
    }

    // ─── NHÓM 5: Edge cases ──────────────────────────────────────────────────

    @Test
    @DisplayName("[TC-14] Tên task rỗng vẫn tạo được Task (không throw exception)")
    void testTask_EmptyName_NoException() {
        assertDoesNotThrow(() -> {
            Task emptyNameTask = new Task("", "TODO", "x@y.com", LocalDateTime.now().plusDays(1));
            assertEquals("", emptyNameTask.getName());
        });
    }

    @Test
    @DisplayName("[TC-15] Hai Task khác nhau — độc lập, không ảnh hưởng lẫn nhau")
    void testTask_TwoInstances_Independent() {
        Task task2 = new Task("Task 2", "DONE", "z@w.com", LocalDateTime.now().plusDays(2));
        task.setStatus("IN_PROGRESS");

        // task2 không bị ảnh hưởng
        assertEquals("DONE", task2.getStatus(),
                "Thay đổi task 1 không được ảnh hưởng task 2");
        assertEquals("IN_PROGRESS", task.getStatus());
    }
}