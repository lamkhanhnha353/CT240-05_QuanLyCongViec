package com.teamwork.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LCK (Lock/Edge-case) Test cho PasswordUtil
 *
 * Tập trung kiểm tra các trường hợp biên, kịch bản đặc biệt
 * và đảm bảo tính bất biến (immutability) của hàm băm.
 *
 * Không cần Database.
 */
@DisplayName("LCK Tests - PasswordUtil (Edge Cases & Security)")
public class LCKPasswordUtilTest {

    // ─── NHÓM 1: Tính bất biến (Immutability) ────────────────────────────────

    @Test
    @DisplayName("[LCK-01] Hash không thay đổi dù gọi song song (thread-safety)")
    void testHashPassword_ThreadSafety() throws InterruptedException {
        final String password = "concurrentTest";
        final String expected = PasswordUtil.hashPassword(password);
        final String[] results = new String[10];
        final Thread[] threads = new Thread[10];

        for (int i = 0; i < 10; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> results[idx] = PasswordUtil.hashPassword(password));
            threads[i].start();
        }
        for (Thread t : threads)
            t.join();

        for (int i = 0; i < 10; i++) {
            assertEquals(expected, results[i],
                    "Thread " + i + " phải cho ra cùng hash với thread chính");
        }
    }

    @Test
    @DisplayName("[LCK-02] Hash là chuỗi chỉ chứa ký tự Base64 hợp lệ")
    void testHashPassword_OutputIsValidBase64() {
        String hash = PasswordUtil.hashPassword("testpassword");
        // Base64 chỉ chứa: A-Z, a-z, 0-9, +, /, =
        assertTrue(hash.matches("^[A-Za-z0-9+/=]+$"),
                "Hash phải chỉ chứa ký tự Base64 hợp lệ");
    }

    // ─── NHÓM 2: Parameterized Tests (nhiều input) ───────────────────────────

    @ParameterizedTest(name = "[LCK-03] hashPassword(''{0}'') không null và dài 44 ký tự")
    @ValueSource(strings = { "a", "123", "abc123", "P@$$w0rd!", "very_long_password_12345" })
    @DisplayName("[LCK-03] Nhiều mật khẩu khác nhau đều cho hash 44 ký tự")
    void testHashPassword_VariousInputs_Always44Chars(String password) {
        String hash = PasswordUtil.hashPassword(password);
        assertNotNull(hash, "Hash của '" + password + "' không được null");
        assertEquals(44, hash.length(),
                "Hash của '" + password + "' phải có đúng 44 ký tự");
    }

    @ParameterizedTest(name = "[LCK-04] ''{0}'' và ''{0} '' (thêm dấu cách) → hash khác nhau")
    @ValueSource(strings = { "abc", "pass", "12345" })
    @DisplayName("[LCK-04] Khoảng trắng cuối chuỗi tạo ra hash khác (sensitive to trailing space)")
    void testHashPassword_TrailingSpace_DifferentHash(String password) {
        String hashWithout = PasswordUtil.hashPassword(password);
        String hashWith = PasswordUtil.hashPassword(password + " ");
        assertNotEquals(hashWithout, hashWith,
                "Khoảng trắng cuối phải tạo ra hash khác nhau");
    }

    // ─── NHÓM 3: Kiểm tra đặc tính Salt ─────────────────────────────────────

    @Test
    @DisplayName("[LCK-05] Mật khẩu ngắn 1 ký tự vẫn tạo hash 44 ký tự")
    void testHashPassword_SingleChar_HashLength44() {
        String hash = PasswordUtil.hashPassword("a");
        assertEquals(44, hash.length(),
                "Mật khẩu 1 ký tự cũng phải ra hash 44 ký tự (do SHA-256 fixed output)");
    }

    @Test
    @DisplayName("[LCK-06] '1' và '1' + SALT → hash khác với SHA-256 không salt")
    void testHashPassword_SaltChangesOutput() {
        // SHA-256 thuần của "1" =
        // "6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b"
        // Với SALT "TeamworkMaster@2026", kết quả phải khác
        String hashWithSalt = PasswordUtil.hashPassword("1");
        String sha256PureOf1 = "a4ayc/80/OGda4BO/1o/V0etpOqiLx1JwB5S3beHW0s="; // Base64 của SHA-256("1")
        assertNotEquals(sha256PureOf1, hashWithSalt,
                "SALT phải làm thay đổi hash so với SHA-256 không salt");
    }

    @Test
    @DisplayName("[LCK-07] Hai mật khẩu chỉ khác 1 ký tự → hash hoàn toàn khác nhau")
    void testHashPassword_OneCharDiff_CompletelyDifferentHash() {
        String hash1 = PasswordUtil.hashPassword("password1");
        String hash2 = PasswordUtil.hashPassword("password2");
        // SHA-256 có tính chất avalanche: thay 1 bit → ~50% bit thay đổi
        assertNotEquals(hash1, hash2);
        // Kiểm tra không có prefix chung quá dài (ít nhất 10 ký tự đầu khác)
        int commonPrefixLen = 0;
        for (int i = 0; i < Math.min(hash1.length(), hash2.length()); i++) {
            if (hash1.charAt(i) == hash2.charAt(i))
                commonPrefixLen++;
            else
                break;
        }
        assertTrue(commonPrefixLen < 10,
                "Hash của 2 mật khẩu gần nhau không được có prefix chung quá dài");
    }

    // ─── NHÓM 4: Bảo mật ──────────────────────────────────────────────────────

    @Test
    @DisplayName("[LCK-08] Hash không tiết lộ thông tin về độ dài mật khẩu gốc")
    void testHashPassword_HidesPasswordLength() {
        String hashShort = PasswordUtil.hashPassword("ab");
        String hashLong = PasswordUtil.hashPassword("abcdefghijklmnopqrstuvwxyz123456");
        // Cả hai đều có 44 ký tự — không thể đoán độ dài mật khẩu gốc qua hash
        assertEquals(hashShort.length(), hashLong.length(),
                "Hash của mật khẩu ngắn và dài phải có cùng độ dài (không lộ info)");
    }

    @Test
    @DisplayName("[LCK-09] Hash không chứa mật khẩu gốc (dù dưới dạng substring)")
    void testHashPassword_DoesNotContainPlaintext() {
        String password = "mypassword123";
        String hash = PasswordUtil.hashPassword(password);
        assertFalse(hash.contains(password),
                "Hash không được chứa mật khẩu gốc dưới dạng substring");
    }

    @Test
    @DisplayName("[LCK-10] Mật khẩu SQL Injection không crash hệ thống")
    void testHashPassword_SqlInjection_NoException() {
        String sqlInjection = "' OR '1'='1'; DROP TABLE TBL_USERS; --";
        assertDoesNotThrow(() -> {
            String hash = PasswordUtil.hashPassword(sqlInjection);
            assertNotNull(hash);
            assertEquals(44, hash.length());
        });
    }

    @Test
    @DisplayName("[LCK-11] Mật khẩu XSS script không crash")
    void testHashPassword_XSSInput_NoException() {
        String xss = "<script>alert('xss')</script>";
        assertDoesNotThrow(() -> {
            String hash = PasswordUtil.hashPassword(xss);
            assertNotNull(hash);
        });
    }

    // ─── NHÓM 5: Workflow đăng ký → đăng nhập ────────────────────────────────

    @Test
    @DisplayName("[LCK-12] Workflow: hash khi đăng ký → verify khi đăng nhập")
    void testHashPassword_RegistrationLoginWorkflow() {
        String plainPassword = "MySecurePass@2026";

        // Bước 1: Đăng ký — băm mật khẩu để lưu vào DB
        String storedHash = PasswordUtil.hashPassword(plainPassword);

        // Bước 2: Đăng nhập — băm lại để so sánh
        String loginHash = PasswordUtil.hashPassword(plainPassword);

        assertEquals(storedHash, loginHash,
                "Hash lúc đăng ký và đăng nhập phải khớp nhau");
    }

    @Test
    @DisplayName("[LCK-13] Workflow: mật khẩu sai → hash khác → đăng nhập thất bại")
    void testHashPassword_WrongPasswordWorkflow() {
        String correctPassword = "CorrectPass123";
        String wrongPassword = "WrongPass456";

        String storedHash = PasswordUtil.hashPassword(correctPassword);
        String loginHash = PasswordUtil.hashPassword(wrongPassword);

        assertNotEquals(storedHash, loginHash,
                "Mật khẩu sai phải cho hash khác → đăng nhập thất bại");
    }
}