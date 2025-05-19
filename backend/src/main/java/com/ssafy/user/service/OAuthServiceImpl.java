package com.ssafy.user.service;

import com.ssafy.exception.BusinessException;
import com.ssafy.exception.ErrorCode;
import com.ssafy.user.dto.KakaoTokenRes;
import com.ssafy.user.dto.KakaoUserInfoRes;
import com.ssafy.user.dto.ReadTokenResDto;
import com.ssafy.user.dto.ReadUserResDto;
import com.ssafy.user.mapper.OAuthMapper;
import com.ssafy.user.mapper.UserMapper;
import com.ssafy.user.vo.LoginType;
import com.ssafy.user.vo.OAuthEntity;
import com.ssafy.user.vo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final KakaoOAuthClient kakaoOauthClient;
    private final OAuthMapper oauthMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ReadTokenResDto readToken(String code) {
        if (code == null || code.isEmpty()) {
            throw new BusinessException(ErrorCode.KAKAO_OAUTH_AUTHORIZATION_CODE_MISSING);
        }

        // 카카오 API를 통해 토큰과 사용자 정보 가져오기
        KakaoTokenRes kakaoTokenRes = kakaoOauthClient.getToken(code);
        KakaoUserInfoRes kakaoUserInfoRes = kakaoOauthClient.getUserInfo(kakaoTokenRes.getAccessToken());
        String kakaoId = kakaoUserInfoRes.getId();
        String profileImage = kakaoUserInfoRes.getProperties().get("profile_image"); // 프로필 이미지 URL 가져오기

        String userId;
        if (oauthMapper.existKakaoUser(kakaoId) == 0) { // 0이면 존재하지 않음, 1 이상이면 존재함
            // 새로운 사용자의 경우, User 테이블에 새로운 ID 생성 및 삽입
            Integer maxUserId = userMapper.getMaxUserId();
            userId = String.valueOf(maxUserId + 1);

            // User 테이블에 새로운 사용자 삽입
            User newUser = new User();
            newUser.setId(userId);
            newUser.setEmail(kakaoId);
            newUser.setNickname(kakaoUserInfoRes.getProperties().get("nickname"));
            newUser.setLoginType(LoginType.SOCIAL);
            newUser.setVerified(true);
            newUser.setProfileImage(kakaoUserInfoRes.getProperties().get("profile_image")); // 프로필 이미지 저장
            userMapper.registerUser(newUser);

            // OAuthEntity 객체 생성 및 설정
            OAuthEntity oauthEntity = new OAuthEntity();
            oauthEntity.setKakaoId(kakaoId);
            oauthEntity.setAccessToken(kakaoTokenRes.getAccessToken());
            oauthEntity.setRefreshToken(kakaoTokenRes.getRefreshToken());
            oauthEntity.setProfileImage(kakaoUserInfoRes.getProperties().get("profile_image"));
            oauthEntity.setUser(newUser);

            // OAuthEntity 테이블에 삽입
            oauthMapper.insertKakaoUser(oauthEntity);
        } else {
            // 기존 사용자일 경우 userId를 조회하고 OAuthEntity의 토큰만 업데이트
            userId = userMapper.findUserIdByKakaoId(kakaoId);
            oauthMapper.updateKakaoUser(kakaoTokenRes.getAccessToken(), kakaoTokenRes.getRefreshToken(), userId);

            userMapper.updateProfileImage(userId, profileImage);
        }

        return new ReadTokenResDto(kakaoTokenRes.getAccessToken(), kakaoTokenRes.getRefreshToken());
    }





    @Override
    public ReadUserResDto readMember(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new BusinessException(ErrorCode.KAKAO_OAUTH_ACCESS_TOKEN_MISSING);
        }

        accessToken = accessToken.replaceFirst("Bearer ", "");
        try {
            KakaoUserInfoRes kakaoUserInfoRes = kakaoOauthClient.getUserInfo(accessToken);
            return new ReadUserResDto(kakaoUserInfoRes.getProperties().get("nickname"));

        } catch (WebClientResponseException e) {
            handleWebClientException(e);
            throw new BusinessException(ErrorCode.UNEXPECTED_EXCEPTION); // 추가된 부분
        }
    }

    private void handleWebClientException(WebClientResponseException e) {
        if (HttpStatus.UNAUTHORIZED.equals(e.getStatusCode())) {
            throw new BusinessException(ErrorCode.KAKAO_OAUTH_ACCESS_TOKEN_INVALID);
        } else if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
            throw new BusinessException(ErrorCode.KAKAO_OAUTH_ACCESS_TOKEN_USER_NOT_FOUND);
        } else {
            throw new BusinessException(ErrorCode.UNEXPECTED_EXCEPTION);
        }
    }

    // 로그아웃 메서드 추가
    public boolean logout(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new BusinessException(ErrorCode.KAKAO_OAUTH_ACCESS_TOKEN_MISSING);
        }

        // KakaoOAuthClient의 logout 메서드를 호출
        return kakaoOauthClient.logout(accessToken);
    }


}
