package com.ssafy.user.vo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "user_id") // 외래 키 설정
    private User user;

    private String kakaoId;

    private String accessToken;

    private String refreshToken;
    private String profileImage; // 프로필 이미지 URL

    public void update(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getProfileImage() {
        return profileImage;
    }
}
