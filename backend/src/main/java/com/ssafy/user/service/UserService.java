package com.ssafy.user.service;

import com.ssafy.user.vo.OAuthEntity;
import com.ssafy.user.vo.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    void registerUser(User user);
    User loginUser(String email, String password);
    void updateUser(User user);
    User getUserById(String id);
    boolean sendVerificationCode(String email);
    boolean verifyCode(String email, String code);
    void processKakaoLogin(User user, OAuthEntity oauthEntity);
    User findUserByEmail(String email);
    String saveProfileImage(String id, MultipartFile file);
    void updateUserInfo(String id, String newNickname, String newPassword);
}
