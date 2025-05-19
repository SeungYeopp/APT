package com.ssafy.user.service;


import com.ssafy.user.dto.ReadTokenResDto;
import com.ssafy.user.dto.ReadUserResDto;

public interface OAuthService {
    ReadTokenResDto readToken(String code);
    ReadUserResDto readMember(String accessToken);
    boolean logout(String accessToken);
}
