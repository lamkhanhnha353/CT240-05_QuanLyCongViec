package com.teamwork.utils;

import java.security.MessageDigest;
import java.util.Base64;

public class PasswordUtil {
    
    private static final String SALT = "TeamworkMaster@2026";

    public static String hashPassword(String plainPassword) {
        try {
            String saltedPassword = plainPassword + SALT;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(saltedPassword.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi mã hóa mật khẩu", ex);
        }
    }
}