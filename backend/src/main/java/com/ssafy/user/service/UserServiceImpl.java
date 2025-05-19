package com.ssafy.user.service;

import com.ssafy.user.mapper.OAuthMapper;
import com.ssafy.user.mapper.UserMapper;
import com.ssafy.user.vo.*;
import com.ssafy.util.PasswordUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final OAuthMapper oauthMapper;
    private final JavaMailSender mailSender;

    @Transactional
    public void processKakaoLogin(User user, OAuthEntity oauthEntity) {
        // User 테이블에 존재하지 않으면 삽입
        Integer maxUserId = userMapper.getMaxUserId(); // User 테이블의 최대 ID 조회
        user.setId(String.valueOf(maxUserId + 1));     // ID 값을 최대 ID + 1로 설정

        userMapper.registerUser(user);

        // OAuthEntity에 새 User ID 설정 후 삽입
        oauthEntity.setUser(user);
        oauthMapper.insertKakaoUser(oauthEntity);
    }

    // 회원가입
    @Transactional
    @Override
    public void registerUser(User user) {
        // 이메일 인증 여부 확인
        User existingUser = userMapper.getUserByEmail(user.getEmail());
        if (existingUser == null || !existingUser.isVerified()) {
            throw new IllegalStateException("Email not verified. Please verify your email before registering.");
        }

        // 비밀번호 해싱 및 저장
        byte[] salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hashPassword(user.getPassword(), salt);

        user.setId(existingUser.getId()); // 인증 시 생성된 ID 사용
        user.setPassword(hashedPassword);
        user.setVerified(true);
        user.setSalt(Base64.getEncoder().encodeToString(salt));
        user.setNickname(user.getNickname());
        user.setLoginType(LoginType.REGULAR);
        user.setRole(UserRole.USER);

        userMapper.updateUser(user); // 정보 업데이트
    }

    @Override
    public User loginUser(String email, String password) {
        User user = userMapper.getUserByEmail(email);
        if (user != null && user.isVerified()) {
            byte[] salt = Base64.getDecoder().decode(user.getSalt());
            boolean isPasswordCorrect = PasswordUtils.verifyPassword(password, user.getPassword(), salt);

            if (isPasswordCorrect) {
                return user;
            }
        }
        return null; // 인증되지 않았거나 비밀번호 불일치
    }

    @Transactional
    @Override
    public void updateUser(User user) {
        byte[] salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hashPassword(user.getPassword(), salt);
        user.setPassword(hashedPassword);
        user.setSalt(Base64.getEncoder().encodeToString(salt));

        userMapper.updateUser(user);
    }

    @Override
    public User getUserById(String id) {
        return userMapper.getUserById(id);
    }

    @Override
    public User findUserByEmail(String email) {
        return userMapper.findUserIdByEmail(email);
    }

    // 인증 코드 전송
    @Override
    public boolean sendVerificationCode(String email) {
        String code = generateCode();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your verification code");
            message.setText("Your verification code is " + code);
            mailSender.send(message);

            VerificationCode verificationCode = new VerificationCode(email, code);
            userMapper.saveVerificationCode(verificationCode);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 이메일 인증 코드 검증 및 회원 데이터 저장
    @Transactional
    @Override
    public boolean verifyCode(String email, String code) {
        VerificationCode verificationCode = userMapper.getVerificationCodeByEmail(email);

        if (verificationCode != null && verificationCode.getCode().equals(code)) {
            // 이메일로 User 조회
            User user = userMapper.getUserByEmail(email);

            // User가 존재하지 않으면 새로 생성
            if (user == null) {
                user = new User();
                Integer maxUserId = userMapper.getMaxUserId(); // User 테이블의 최대 ID 조회
                user.setId(String.valueOf(maxUserId + 1));     // ID 값을 최대 ID + 1로 설정
                user.setEmail(email);
                user.setVerified(true); // 인증 상태 설정
                user.setLoginType(LoginType.REGULAR);
                userMapper.registerUser(user); // 새 User 등록
            } else {
                user.setVerified(true); // 기존 User 인증 상태 설정
                userMapper.updateUser(user);
            }

            // 인증 코드 삭제
            userMapper.deleteVerificationCodeByEmail(email);
            return true;
        }
        return false;
    }

    @Override
    public String saveProfileImage(String userId, MultipartFile file) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean isCreated = directory.mkdirs();
                if (!isCreated) {
                    throw new IOException("Failed to create directory for profile images");
                }
            }

            // 파일 저장
            String fileName = userId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File targetFile = new File(uploadDir, fileName);
            file.transferTo(targetFile);

            // 파일 URL 생성
            String imageUrl = "/uploads/" + fileName;

            // DB 업데이트 호출
            userMapper.updateProfileImage(userId, imageUrl);

            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile image: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void updateUserInfo(String id, String newNickname, String newPassword) {
        User user = userMapper.getUserById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        byte[] salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hashPassword(newPassword, salt);

        user.setNickname(newNickname);
        user.setPassword(hashedPassword);
        user.setSalt(Base64.getEncoder().encodeToString(salt));

        userMapper.updateUser2(user.getId(), user.getNickname(), user.getPassword(), user.getSalt());

    }





    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}