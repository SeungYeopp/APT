package com.ssafy.user.controller;

import com.ssafy.user.dto.*;
import com.ssafy.user.service.KakaoOAuthClient;
import com.ssafy.user.service.OAuthService;
import com.ssafy.user.service.UserService;
import com.ssafy.user.vo.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
@Slf4j
public class OAuthController {

    private final KakaoOAuthClient kakaoOauthClient;
    private final OAuthService oauthService;
    private final UserService userService;

    @PostMapping("/auth")
    public ResponseEntity<?> readToken(
            @RequestBody ReadTokenReq req, HttpSession session) {

        ReadTokenResDto resDto = oauthService.readToken(req.getCode());
        KakaoUserInfoRes kakaoUserInfoRes = kakaoOauthClient.getUserInfo(resDto.getAccessToken());
        String kakaoId = kakaoUserInfoRes.getId();
        User user = userService.findUserByEmail(kakaoId);

        session.setAttribute("user", user);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", resDto.getAccessToken());
        response.put("refreshToken", resDto.getRefreshToken());
        response.put("user", user);


        return ResponseEntity.ok()
                .header("Set-Cookie", "RefreshToken=" + resDto.getRefreshToken() + "; Path=/; HttpOnly; Secure; SameSite=None")
                .body(response);
    }

    @GetMapping("/member")
    public ResponseEntity<ReadMemberRes> readMember(
            @RequestHeader(value = "Authorization", required = false) String accessToken) {

        ReadUserResDto resDto = oauthService.readMember(accessToken);

        return ResponseEntity.ok(
                new ReadMemberRes(resDto.getNickname())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate(); // Invalidate the session
        return ResponseEntity.ok().build(); // Return success response
    }



}