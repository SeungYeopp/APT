package com.ssafy.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {

    // SHA-256 해시 생성 메서드
    public static String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // 솔트 생성 메서드 (16바이트)
    public static byte[] generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    // 비밀번호 확인 메서드
    public static boolean verifyPassword(String enteredPassword, String storedHash, byte[] salt) {
        String hashedEnteredPassword = PasswordUtils.hashPassword(enteredPassword, salt);
        return hashedEnteredPassword.equals(storedHash);
    }
}
