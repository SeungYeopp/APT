package com.ssafy.user.vo;

import com.ssafy.interest.vo.InterestArea;
import com.ssafy.interest.vo.InterestHouse;
import com.ssafy.review.vo.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@Schema(title = "User", description = "사용자 vo")
public class User {

    @Id
    @Schema(description = "사용자 ID", example = "0ys")
    private String id; // 사용자 ID
    @Schema(description = "사용자 ID", example = "0ys")
    private String nickname; // 닉네임
    @Schema(description = "이메일", example = "ysgong@naver.com")
    private String email; // 이메일 주소
    @Schema(description = "비밀번호", example = "1234")
    private String password; // 암호화된 비밀번호
    private String salt; // 비밀번호 해싱에 사용되는 salt 값

    private String profileImage; // 프로필 이미지 URL

    @Enumerated(EnumType.STRING)
    private LoginType loginType; // 로그인 타입 (SOCIAL 또는 REGULAR)

    @Enumerated(EnumType.STRING)
    private UserRole role; // 사용자 역할 (ADMIN 또는 USER)

    private boolean verified; // 이메일 인증 여부

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OAuthEntity oauthEntity; // 소셜 로그인 정보

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InterestHouse> apartmentInterests; // 아파트 관심사 목록

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InterestArea> neighborhoodInterests; // 동네 관심사 목록

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews; // 리뷰 목록 추가

    public User(String id) {
        this.id = id;
    }
}
