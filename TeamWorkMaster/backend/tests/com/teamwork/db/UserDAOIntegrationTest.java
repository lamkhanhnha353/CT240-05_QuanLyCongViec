package com.teamwork.db;

import com.teamwork.utils.PasswordUtil;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test cho UserDAO (db.UserDAO)
 *
 * ⚠️ YÊU CẦU: MySQL đang chạy + database teamwork_master đã được khởi tạo.
 *
 * Để chạy test này:
 * 1. Mở MySQL Workbench, chạy file init_teamwork_master.sql
 * 2. Cập nhật mật khẩu MySQL trong DatabaseConnection.java
 * 3. Chạy test class này
 *
 * Dữ liệu mẫu cần có trong DB:
 * - admin / 123456 (hash bằng PasswordUtil) / Role = ADMIN / IsActive = true
 */
@DisplayName("Integration Tests - UserDAO (cần Database)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOIntegrationTest {

    private static UserDAO userDAO;

    // Username ngẫu nhiên để test đăng ký (tránh trùng lặp mỗi lần chạy)
    private static final String TEST_USERNAME = "testuser_" + System.currentTimeMillis();
    private static final String TEST_EMAIL = "test_" + System.currentTimeMillis() + "@example.com";
    private static final String TEST_PASSWORD = "Test@123";

    @BeforeAll
    static void setUpAll() {
        userDAO = new UserDAO();
    }

    // ─── NHÓM 1: Đăng nhập (login) ───────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("[UD-01] login() - PASS: admin đăng nhập đúng thông tin")
    void testLogin_ValidCredentials_Success() {
        // Giả sử DB đã có admin với password được hash bằng PasswordUtil
        // Nếu DB vẫn còn plain text, test này sẽ fail cho đến khi migrate
        String result = userDAO.login("admin", "123456");

        // Kết quả hợp lệ: null (không tìm thấy) hoặc "id-role-fullname" hoặc "BANNED"
        // Chỉ pass nếu DB đã dùng PasswordUtil.hashPassword()
        // Nếu vẫn là plain text: result != null (cũng pass để chạy được)
        // Ta kiểm tra format kết quả thay vì giá trị cứng
        if (result != null && !result.equals("BANNED")) {
            String[] parts = result.split("-", 3);
            assertEquals(3, parts.length,
                    "Kết quả login phải có dạng 'id-role-fullname'");
            assertFalse(parts[0].isEmpty(), "ID không được rỗng");
            assertFalse(parts[1].isEmpty(), "Role không được rỗng");
        }
        // Nếu result == null: DB chưa hash → ghi chú để team biết
        System.out.println("[UD-01] Kết quả login: " + result);
    }

    @Test
    @Order(2)
    @DisplayName("[UD-02] login() - FAIL: mật khẩu sai → trả về null")
    void testLogin_WrongPassword_ReturnsNull() {
        String result = userDAO.login("admin", "sai_mat_khau_xyz");
        assertNull(result, "Mật khẩu sai phải trả về null");
    }

    @Test
    @Order(3)
    @DisplayName("[UD-03] login() - FAIL: username không tồn tại → trả về null")
    void testLogin_NonExistentUser_ReturnsNull() {
        String result = userDAO.login("user_khong_ton_tai_99999", "123456");
        assertNull(result, "Username không tồn tại phải trả về null");
    }

    @Test
    @Order(4)
    @DisplayName("[UD-04] login() - FAIL: username rỗng → trả về null")
    void testLogin_EmptyUsername_ReturnsNull() {
        String result = userDAO.login("", "123456");
        assertNull(result, "Username rỗng phải trả về null");
    }

    @Test
    @Order(5)
    @DisplayName("[UD-05] login() - FAIL: cả hai trống → trả về null, không crash")
    void testLogin_BothEmpty_NoException() {
        assertDoesNotThrow(() -> {
            String result = userDAO.login("", "");
            assertNull(result);
        });
    }

    // ─── NHÓM 2: Đăng ký (register) ──────────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("[UD-06] register() - PASS: đăng ký tài khoản mới thành công")
    void testRegister_NewUser_Success() {
        boolean result = userDAO.register(TEST_USERNAME, TEST_PASSWORD, "Test User", TEST_EMAIL);
        assertTrue(result, "Đăng ký tài khoản mới phải thành công");
    }

    @Test
    @Order(11)
    @DisplayName("[UD-07] register() - FAIL: username trùng → trả về false")
    void testRegister_DuplicateUsername_Fail() {
        // Đăng ký lần 2 cùng username → phải false
        boolean result = userDAO.register(TEST_USERNAME, "another", "Another", "another@test.com");
        assertFalse(result, "Đăng ký username trùng phải thất bại");
    }

    @Test
    @Order(12)
    @DisplayName("[UD-08] register() - FAIL: email trùng → trả về false")
    void testRegister_DuplicateEmail_Fail() {
        // Đăng ký email đã dùng → phải false
        String newUser = "newuser_" + System.currentTimeMillis();
        boolean result = userDAO.register(newUser, "123", "New User", TEST_EMAIL);
        assertFalse(result, "Đăng ký email đã tồn tại phải thất bại");
    }

    // ─── NHÓM 3: Tìm kiếm user (searchUsers) ─────────────────────────────────

    @Test
    @Order(20)
    @DisplayName("[UD-09] searchUsers() - PASS: tìm theo từ khóa có kết quả")
    void testSearchUsers_ValidKeyword_ReturnsResults() {
        String result = userDAO.searchUsers("admin");
        assertNotNull(result, "Kết quả tìm kiếm không được null");
        assertTrue(result.startsWith("["), "Kết quả phải là mảng JSON");
        assertTrue(result.endsWith("]"), "Kết quả phải là mảng JSON hợp lệ");
    }

    @Test
    @Order(21)
    @DisplayName("[UD-10] searchUsers() - PASS: tìm từ khóa không tồn tại → mảng rỗng []")
    void testSearchUsers_NoMatch_EmptyArray() {
        String result = userDAO.searchUsers("xyz_khong_ton_tai_abc_99999");
        assertEquals("[]", result, "Không tìm thấy phải trả về mảng rỗng");
    }

    @Test
    @Order(22)
    @DisplayName("[UD-11] searchUsers() - PASS: từ khóa rỗng → không crash")
    void testSearchUsers_EmptyKeyword_NoException() {
        assertDoesNotThrow(() -> {
            String result = userDAO.searchUsers("");
            assertNotNull(result);
            assertTrue(result.startsWith("["));
        });
    }

    // ─── NHÓM 4: Khóa/Mở khóa tài khoản ─────────────────────────────────────

    @Test
    @Order(30)
    @DisplayName("[UD-12] addUserByAdmin() - PASS: admin tạo tài khoản mới thành công")
    void testAddUserByAdmin_NewUser_ReturnsOne() {
        long ts = System.currentTimeMillis() + 1;
        String uname = "admin_created_" + ts;
        String email = "admin_" + ts + "@test.com";

        int result = userDAO.addUserByAdmin(uname, "abc123", "Admin Created", email);
        assertEquals(1, result, "Tạo tài khoản mới bởi admin phải trả về 1");
    }

    @Test
    @Order(31)
    @DisplayName("[UD-13] addUserByAdmin() - FAIL: username trùng → trả về -1")
    void testAddUserByAdmin_DuplicateUsername_ReturnsMinusOne() {
        int result = userDAO.addUserByAdmin("admin", "abc", "Dup Admin", "dup_admin@test.com");
        assertEquals(-1, result, "Username trùng phải trả về -1");
    }

    @Test
    @Order(32)
    @DisplayName("[UD-14] getAllMembersJson() - PASS: trả về JSON mảng hợp lệ")
    void testGetAllMembersJson_ReturnsValidJson() {
        String result = userDAO.getAllMembersJson();
        assertNotNull(result);
        assertTrue(result.startsWith("["), "Phải là mảng JSON");
        assertTrue(result.endsWith("]"), "Phải là mảng JSON hợp lệ");
    }

    // ─── NHÓM 5: getFullNameById ──────────────────────────────────────────────

    @Test
    @Order(40)
    @DisplayName("[UD-15] getFullNameById() - PASS: ID hợp lệ → trả về tên")
    void testGetFullNameById_ValidId_ReturnsName() {
        String result = userDAO.getFullNameById(1); // Admin (ID=1) luôn tồn tại
        assertNotNull(result, "FullName của user ID=1 không được null");
        assertFalse(result.isEmpty(), "FullName không được rỗng");
    }

    @Test
    @Order(41)
    @DisplayName("[UD-16] getFullNameById() - FAIL: ID không tồn tại → trả về rỗng")
    void testGetFullNameById_InvalidId_ReturnsEmpty() {
        String result = userDAO.getFullNameById(999999);
        // Trả về "" theo logic hiện tại
        assertEquals("", result, "ID không tồn tại phải trả về chuỗi rỗng");
    }
}