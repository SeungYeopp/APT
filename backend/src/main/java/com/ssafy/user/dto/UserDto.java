package com.ssafy.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "UserDto (사용자 Dto)", description = "회원가입에 필요한 기본 사용자 Dto")
public class UserDto {
    @Id
    @Schema(description = "사용자 ID", example = "0ys")
    private String id; // 사용자 ID
    @Schema(description = "닉네임", example = "0ys")
    private String nickname; // 닉네임
    @Schema(description = "이메일", example = "ysgong@naver.com")
    private String email; // 이메일 주소
    @Schema(description = "비밀번호", example = "1234")
    private String password; // 암호화된 비밀번호
}
