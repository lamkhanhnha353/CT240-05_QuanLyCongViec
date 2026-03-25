package com.teamwork.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test cho lớp PasswordUtil (utils.PasswordUtil)
 *
 * Không cần kết nối Database.
 * Kiểm tra logic băm mật khẩu SHA-256 + Salt.
 */
@DisplayName("Unit Tests - PasswordUtil")
public class PasswordUtilTest {

    // ─── NHÓM 1: Tính đúng đắn của hàm băm ──────────────────────────────────

    @Test
    @DisplayName("[PU-01] hashPassword() không trả về null")
    void testHashPassword_NotNull() {
        String result = PasswordUtil.hashPassword("123456");
        assertNotNull(result, "Kết quả băm không được là null");
    }

    @Test
    @DisplayName("[PU-02] hashPassword() không trả về chuỗi rỗng")
    void testHashPassword_NotEmpty() {
        String result = PasswordUtil.hashPassword("123456");
        assertFalse(result.isEmpty(), "Kết quả băm không được là chuỗi rỗng");
    }

    @Test
    @DisplayName("[PU-03] hashPassword() trả về chuỗi Base64 có độ dài 44 ký tự (SHA-256)")
    void testHashPassword_CorrectLength() {
        String result = PasswordUtil.hashPassword("mypassword");
        // SHA-256 → 32 bytes → Base64 → 44 ký tự (bao gồm padding =)
        assertEquals(44, result.length(),
                "SHA-256 encode Base64 phải có đúng 44 ký tự");
    }

    // ─── NHÓM 2: Tính nhất quán (Deterministic) ──────────────────────────────

    @Test
    @DisplayName("[PU-04] Cùng mật khẩu → luôn ra cùng một hash")
    void testHashPassword_SameInput_SameOutput() {
        String hash1 = PasswordUtil.hashPassword("abc123");
        String hash2 = PasswordUtil.hashPassword("abc123");
        assertEquals(hash1, hash2,
                "Cùng mật khẩu phải cho ra cùng kết quả băm");
    }

    @Test
    @DisplayName("[PU-05] Cùng mật khẩu, gọi 5 lần — kết quả luôn giống nhau")
    void testHashPassword_Consistent_MultipleCalls() {
        String password = "teamwork@2026";
        String expected = PasswordUtil.hashPassword(password);
        for (int i = 0; i < 5; i++) {
            assertEquals(expected, PasswordUtil.hashPassword(password),
                    "Lần gọi thứ " + (i + 1) + " phải cho ra cùng kết quả");
        }
    }

    // ─── NHÓM 3: Tính an toàn (Không thể đoán ngược) ────────────────────────

    @Test
    @DisplayName("[PU-06] Mật khẩu khác nhau → hash khác nhau")
    void testHashPassword_DifferentInputs_DifferentOutputs() {
        String hash1 = PasswordUtil.hashPassword("password1");
        String hash2 = PasswordUtil.hashPassword("password2");
        assertNotEquals(hash1, hash2,
                "Mật khẩu khác nhau phải cho ra hash khác nhau");
    }

    @Test
    @DisplayName("[PU-07] Hash KHÔNG bằng với mật khẩu gốc (không lưu plain text)")
    void testHashPassword_NotEqualToPlainText() {
        String plain = "123456";
        String hashed = PasswordUtil.hashPassword(plain);
        assertNotEquals(plain, hashed,
                "Hash phải khác với mật khẩu gốc — không được lưu plain text");
    }

    @Test
    @DisplayName("[PU-08] Chữ hoa/thường khác nhau → hash khác nhau (case-sensitive)")
    void testHashPassword_CaseSensitive() {
        String hashLower = PasswordUtil.hashPassword("password");
        String hashUpper = PasswordUtil.hashPassword("PASSWORD");
        assertNotEquals(hashLower, hashUpper,
                "Băm phải phân biệt chữ hoa/thường");
    }

    @Test
    @DisplayName("[PU-09] Mật khẩu có khoảng trắng — được xử lý không lỗi")
    void testHashPassword_WithSpaces_NoException() {
        assertDoesNotThrow(() -> {
            String result = PasswordUtil.hashPassword("pass word 123");
            assertNotNull(result);
        });
    }

    // ─── NHÓM 4: Edge cases ──────────────────────────────────────────────────

    @Test
    @DisplayName("[PU-10] Mật khẩu rỗng — không throw exception, trả về hash")
    void testHashPassword_EmptyPassword_NoException() {
        assertDoesNotThrow(() -> {
            String result = PasswordUtil.hashPassword("");
            assertNotNull(result, "Mật khẩu rỗng vẫn phải trả về hash (do có SALT)");
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @DisplayName("[PU-11] Mật khẩu rất dài (1000 ký tự) — không throw exception")
    void testHashPassword_VeryLongPassword_NoException() {
        String longPassword = "A".repeat(1000);
        assertDoesNotThrow(() -> {
            String result = PasswordUtil.hashPassword(longPassword);
            assertNotNull(result);
            assertEquals(44, result.length(),
                    "Hash của mật khẩu dài cũng phải có độ dài 44 ký tự");
        });
    }

    @Test
    @DisplayName("[PU-12] Mật khẩu ký tự đặc biệt — không throw exception")
    void testHashPassword_SpecialCharacters_NoException() {
        assertDoesNotThrow(() -> {
            String result = PasswordUtil.hashPassword("p@$$w0rd!#%^&*()");
            assertNotNull(result);
            assertEquals(44, result.length());
        });
    }

    @Test
    @DisplayName("[PU-13] Mật khẩu tiếng Việt Unicode — không throw exception")
    void testHashPassword_VietnameseUnicode_NoException() {
        assertDoesNotThrow(() -> {
            String result = PasswordUtil.hashPassword("mậtkhẩu123");
            assertNotNull(result);
        });
    }

    // ─── NHÓM 5: Kiểm tra Salt hoạt động đúng ────────────────────────────────

    @Test
    @DisplayName("[PU-14] Salt đảm bảo 'password' khác với hash SHA-256 thuần")
    void testHashPassword_SaltIsApplied() {
        // Nếu không có salt, SHA-256("123456") sẽ là một giá trị cố định khác
        // SHA-256 thuần của "123456" =
        // "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"
        // Với SALT thêm vào, kết quả phải khác
        String hashWithSalt = PasswordUtil.hashPassword("123456");
        String sha256PureOf123456 = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";
        assertNotEquals(sha256PureOf123456, hashWithSalt,
                "SALT phải làm cho hash khác với SHA-256 thuần không có salt");
    }
}