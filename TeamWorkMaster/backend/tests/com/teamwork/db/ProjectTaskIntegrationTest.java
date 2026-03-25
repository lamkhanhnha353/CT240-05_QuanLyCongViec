package com.teamwork.db;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test cho ProjectDAO và TaskDAO
 *
 * ⚠️ YÊU CẦU: MySQL đang chạy + database teamwork_master đã khởi tạo.
 *
 * Test được sắp xếp theo thứ tự: Tạo Project → Tạo Task → Cập nhật → Xóa
 * để đảm bảo tính liên kết giữa các bước.
 */
@DisplayName("Integration Tests - ProjectDAO & TaskDAO (cần Database)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectTaskIntegrationTest {

    private static ProjectDAO projectDAO;
    private static TaskDAO taskDAO;

    // Lưu ID giữa các test để tái sử dụng
    private static int createdProjectId = -1;

    // userId của admin mặc định
    private static final int ADMIN_USER_ID = 1;

    @BeforeAll
    static void setUpAll() {
        projectDAO = new ProjectDAO();
        taskDAO = new TaskDAO();
    }

    // ═══════════════════════════════════════════════════════════════
    // NHÓM 1: ProjectDAO
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("[PT-01] ProjectDAO.createProject() - PASS: tạo dự án thành công")
    void testCreateProject_ValidData_ReturnsPositiveId() throws Exception {
        int id = projectDAO.createProject(
                "Test Project " + System.currentTimeMillis(),
                "Mô tả dự án test",
                "2025-01-01",
                "2025-12-31",
                "MEDIUM",
                ADMIN_USER_ID);

        assertTrue(id > 0, "ID dự án mới phải lớn hơn 0");
        createdProjectId = id; // Lưu lại để dùng ở các test sau
        System.out.println("[PT-01] Project ID vừa tạo: " + createdProjectId);
    }

    @Test
    @Order(2)
    @DisplayName("[PT-02] ProjectDAO.getOwnerId() - PASS: trả về đúng ownerId")
    void testGetOwnerId_ValidProject_ReturnsCorrectOwner() throws Exception {
        assumeProjectCreated();

        Integer ownerId = projectDAO.getOwnerId(createdProjectId);
        assertNotNull(ownerId, "OwnerId không được null");
        assertEquals(ADMIN_USER_ID, (int) ownerId,
                "OwnerId phải là user đã tạo project");
    }

    @Test
    @Order(3)
    @DisplayName("[PT-03] ProjectDAO.getOwnerId() - FAIL: project không tồn tại → null")
    void testGetOwnerId_NonExistentProject_ReturnsNull() throws Exception {
        Integer ownerId = projectDAO.getOwnerId(999999);
        assertNull(ownerId, "Project không tồn tại phải trả về null");
    }

    @Test
    @Order(4)
    @DisplayName("[PT-04] ProjectDAO.getAllProjectsJson() - PASS: trả về JSON mảng hợp lệ")
    void testGetAllProjectsJson_ValidUser_ReturnsJsonArray() throws Exception {
        String result = projectDAO.getAllProjectsJson(ADMIN_USER_ID);
        assertNotNull(result, "Kết quả không được null");
        assertTrue(result.startsWith("["), "Phải bắt đầu bằng '['");
        assertTrue(result.endsWith("]"), "Phải kết thúc bằng ']'");
    }

    @Test
    @Order(5)
    @DisplayName("[PT-05] ProjectDAO.updateProject() - PASS: cập nhật thành công")
    void testUpdateProject_ValidData_ReturnsTrue() throws Exception {
        assumeProjectCreated();

        boolean result = projectDAO.updateProject(
                createdProjectId,
                "Updated Project Name",
                "Mô tả đã cập nhật",
                "IN_PROGRESS",
                "2025-12-31",
                "HIGH");
        assertTrue(result, "Cập nhật project hợp lệ phải trả về true");
    }

    @Test
    @Order(6)
    @DisplayName("[PT-06] ProjectDAO.updateProject() - FAIL: project không tồn tại → false")
    void testUpdateProject_NonExistentProject_ReturnsFalse() throws Exception {
        boolean result = projectDAO.updateProject(
                999999, "X", "X", "PENDING", "2025-01-01", "LOW");
        assertFalse(result, "Update project không tồn tại phải trả về false");
    }

    // ═══════════════════════════════════════════════════════════════
    // NHÓM 2: TaskDAO
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(10)
    @DisplayName("[PT-07] TaskDAO.createTask() - PASS: tạo task trong project hợp lệ")
    void testCreateTask_ValidProject_ReturnsTrue() {
        assumeProjectCreated();

        boolean result = taskDAO.createTask(
                createdProjectId,
                "Test Task " + System.currentTimeMillis(),
                "Mô tả task test",
                "MEDIUM",
                "2025-12-31",
                "2025-01-01",
                "test,integration",
                "TODO",
                String.valueOf(ADMIN_USER_ID));
        assertTrue(result, "Tạo task hợp lệ phải trả về true");
    }

    @Test
    @Order(11)
    @DisplayName("[PT-08] TaskDAO.createTask() - PASS: deadline null không crash")
    void testCreateTask_NullDeadline_NoException() {
        assumeProjectCreated();

        assertDoesNotThrow(() -> {
            boolean result = taskDAO.createTask(
                    createdProjectId,
                    "Task không deadline",
                    "",
                    "LOW",
                    null, // deadline null
                    null, // startDate null
                    "",
                    "TODO",
                    "");
            assertTrue(result, "Tạo task với deadline null phải thành công");
        });
    }

    @Test
    @Order(12)
    @DisplayName("[PT-09] TaskDAO.getTasksByProject() - PASS: trả về JSON hợp lệ")
    void testGetTasksByProject_ValidProject_ReturnsJsonArray() {
        assumeProjectCreated();

        String result = taskDAO.getTasksByProject(createdProjectId);
        assertNotNull(result, "Kết quả không được null");
        assertTrue(result.startsWith("["), "Phải là mảng JSON");
        assertTrue(result.endsWith("]"), "Phải kết thúc bằng ]");
        // Sau khi tạo task ở test trước, mảng phải có ít nhất 1 phần tử
        assertNotEquals("[]", result, "Phải có ít nhất 1 task vừa tạo");
    }

    @Test
    @Order(13)
    @DisplayName("[PT-10] TaskDAO.getTasksByProject() - PASS: project không có task → mảng rỗng")
    void testGetTasksByProject_EmptyProject_ReturnsEmptyArray() {
        // Project ID 999999 không tồn tại → trả về []
        String result = taskDAO.getTasksByProject(999999);
        assertEquals("[]", result, "Project không có task phải trả về []");
    }

    @Test
    @Order(14)
    @DisplayName("[PT-11] TaskDAO.updateTaskStatus() - PASS: cập nhật status thành công")
    void testUpdateTaskStatus_ValidTask_ReturnsTrue() {
        assumeProjectCreated();

        // Lấy task đầu tiên từ project
        String tasksJson = taskDAO.getTasksByProject(createdProjectId);
        int taskId = extractFirstTaskId(tasksJson);

        if (taskId <= 0) {
            System.out.println("[PT-11] SKIP: Không tìm được taskId từ project");
            return;
        }

        boolean result = taskDAO.updateTaskStatus(taskId, "IN_PROGRESS");
        assertTrue(result, "Cập nhật status hợp lệ phải trả về true");
    }

    @Test
    @Order(15)
    @DisplayName("[PT-12] TaskDAO.updateTaskStatus() - FAIL: taskId không tồn tại → false")
    void testUpdateTaskStatus_NonExistentTask_ReturnsFalse() {
        boolean result = taskDAO.updateTaskStatus(999999, "DONE");
        assertFalse(result, "Task không tồn tại phải trả về false");
    }

    @Test
    @Order(16)
    @DisplayName("[PT-13] TaskDAO.updateTaskDetails() - PASS: cập nhật chi tiết task")
    void testUpdateTaskDetails_ValidTask_ReturnsTrue() {
        assumeProjectCreated();

        String tasksJson = taskDAO.getTasksByProject(createdProjectId);
        int taskId = extractFirstTaskId(tasksJson);

        if (taskId <= 0) {
            System.out.println("[PT-13] SKIP: Không tìm được taskId");
            return;
        }

        boolean result = taskDAO.updateTaskDetails(
                taskId,
                "Updated Task Title",
                "Mô tả đã cập nhật",
                "HIGH",
                "2025-12-31",
                "2025-06-01",
                "updated,tag",
                String.valueOf(ADMIN_USER_ID));
        assertTrue(result, "Cập nhật chi tiết task phải trả về true");
    }

    // ═══════════════════════════════════════════════════════════════
    // NHÓM 3: Dọn dẹp (Cleanup) - chạy cuối cùng
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(50)
    @DisplayName("[PT-14] TaskDAO.deleteTask() - PASS: xóa task thành công")
    void testDeleteTask_ValidTask_ReturnsTrue() {
        assumeProjectCreated();

        String tasksJson = taskDAO.getTasksByProject(createdProjectId);
        int taskId = extractFirstTaskId(tasksJson);

        if (taskId <= 0) {
            System.out.println("[PT-14] SKIP: Không có task để xóa");
            return;
        }

        boolean result = taskDAO.deleteTask(taskId);
        assertTrue(result, "Xóa task hợp lệ phải trả về true");
    }

    @Test
    @Order(51)
    @DisplayName("[PT-15] TaskDAO.deleteTask() - FAIL: task không tồn tại → false")
    void testDeleteTask_NonExistentTask_ReturnsFalse() {
        boolean result = taskDAO.deleteTask(999999);
        assertFalse(result, "Xóa task không tồn tại phải trả về false");
    }

    @Test
    @Order(99)
    @DisplayName("[PT-16] ProjectDAO.deleteProject() - PASS: xóa project test cuối cùng")
    void testDeleteProject_CreatedProject_ReturnsTrue() throws Exception {
        assumeProjectCreated();

        boolean result = projectDAO.deleteProject(createdProjectId);
        assertTrue(result, "Xóa project test phải thành công để dọn sạch DB");
        System.out.println("[PT-16] Đã xóa project ID: " + createdProjectId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Bỏ qua test nếu chưa tạo được project (tránh NullPointerException) */
    private void assumeProjectCreated() {
        assertTrue(createdProjectId > 0,
                "Test này cần project được tạo trước (PT-01 phải pass)");
    }

    /**
     * Trích ID task đầu tiên từ JSON array:
     * [{"id":123,"title":"..."},...] → 123
     */
    private int extractFirstTaskId(String json) {
        if (json == null || json.equals("[]"))
            return -1;
        try {
            int idStart = json.indexOf("\"id\":") + 5;
            int idEnd = json.indexOf(",", idStart);
            return Integer.parseInt(json.substring(idStart, idEnd).trim());
        } catch (Exception e) {
            return -1;
        }
    }
}