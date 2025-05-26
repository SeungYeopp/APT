package com.ssafy.user.controller;

import com.ssafy.user.dto.UserDto;
import com.ssafy.user.service.UserService;
import com.ssafy.user.vo.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "사용자 컨트롤러", description = "회원 등록, 로그인, 로그아웃 등 사용자를 관리합니다.")
public class UserController {

    private final UserService userService;

    // 회원가입 API
    @Operation(summary = "회원가입 API", description = "새로운 사용자를 등록합니다.")
    @Parameter(description = "회원정보.", required = true, content = @Content(schema = @Schema(implementation = UserDto.class)))
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.ok(Collections.singletonMap("message", "User registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(
            @RequestBody User loginRequest,
            HttpSession session) {
        User user = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        if (user != null) {
            session.setAttribute("user", user);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).body(null);
    }

    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("No active session");
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        User user = (User) session.getAttribute("user"); // 세션에서 사용자 정보 가져오기

        if (user == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User is not logged in."));
        }

        // 세션 무효화
        session.invalidate();
        return ResponseEntity.ok(Collections.singletonMap("message", "Logout successful"));
    }

    // 사용자 정보 조회 API
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(404).build();
        }
    }

    // 이메일 인증 코드 전송 API
    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestParam String email) {
        boolean isSent = userService.sendVerificationCode(email);
        if (isSent) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Verification code sent to email."));
        } else {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to send verification code."));
        }
    }

    // 인증 코드 확인 API
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean isVerified = userService.verifyCode(email, code);
        if (isVerified) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Email verified successfully."));
        } else {
            return ResponseEntity.status(400).body(Collections.singletonMap("error", "Invalid verification code."));
        }
    }

    @PostMapping("/profile-image/{id}")
    public ResponseEntity<?> uploadProfileImage(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        try {
            // 1. 파일 업로드 처리
            String imageUrl = userService.saveProfileImage(id, file);

            // 2. 성공 응답 반환
            return ResponseEntity.ok(Collections.singletonMap("profileImage", imageUrl));
        } catch (Exception e) {
            // 3. 에러 응답 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<?> updateUserInfo(
            @PathVariable String id,
            @RequestBody Map<String, String> updateRequest) {
        String newNickname = updateRequest.get("nickname");
        String newPassword = updateRequest.get("password");
        String confirmPassword = updateRequest.get("confirmPassword");

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Passwords do not match."));
        }

        userService.updateUserInfo(id, newNickname, newPassword);
        return ResponseEntity.ok(Collections.singletonMap("message", "User information updated successfully."));
    }



}
