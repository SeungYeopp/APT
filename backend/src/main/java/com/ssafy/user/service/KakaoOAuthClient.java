package com.ssafy.user.service;


import com.ssafy.user.dto.KakaoTokenRes;
import com.ssafy.user.dto.KakaoUserInfoRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private final String clientId = "ad306983234413932d89c70a5829bd65";
    private final String redirectUri = "http://localhost:5173/oauth/redirect";

    public KakaoTokenRes getToken(String code) {
        String url = "https://kauth.kakao.com/oauth/token";

        return WebClient.builder()
                .baseUrl(url)
                .build()
                .post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("redirect_uri", redirectUri)
                        .with("code", code))
                .retrieve()
                .bodyToMono(KakaoTokenRes.class)
                .block();
    }

    public KakaoUserInfoRes getUserInfo(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";

        return WebClient.builder()
                .baseUrl(url)
                .build()
                .get()
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoRes.class)
                .block();
    }

    // 로그아웃 메서드
    public boolean logout(String accessToken) {
        String url = "https://kapi.kakao.com/v1/user/logout";

        try {
            WebClient.builder()
                    .baseUrl(url)
                    .build()
                    .post()
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}