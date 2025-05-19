package com.ssafy.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ValidationException

    // BusinessException
    EMAIL_SEND_FAILURE("B001", "이메일 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_AUTH_INFO_EXPIRED("B002", "이메일 인증 정보가 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    EMAIL_AUTH_CODE_INCORRECT("B002", "이메일 인증번호가 일치하지않습니다.", HttpStatus.UNAUTHORIZED),
    KAKAO_OAUTH_AUTHORIZATION_CODE_MISSING("B003", "카카오 OAUTH 인가코드가 누락되었습니다.", HttpStatus.BAD_REQUEST),
    KAKAO_OAUTH_ACCESS_TOKEN_MISSING("B004", "카카오 OAUTH 액세스 토큰이 누락되었습니다.", HttpStatus.BAD_REQUEST),
    KAKAO_OAUTH_ACCESS_TOKEN_INVALID("B005", "카카오 OAUTH 액세스 토큰이 만료되었거나 유효하지않습니다.", HttpStatus.UNAUTHORIZED),
    KAKAO_OAUTH_ACCESS_TOKEN_USER_NOT_FOUND("B006", "카카오 OAUTH 액세스 토큰으로 조회된 사용자가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    KAKAO_OAUTH_REFRESH_TOKEN_MISSING("B007", "카카오 OAUTH Refresh 토큰이 누락되었습니다.", HttpStatus.BAD_REQUEST),
    KAKAO_OAUTH_REFRESH_TOKEN_INVALID("B008", "카카오 OAUTH Refresh 토큰이 만료되었거나 유효하지않습니다.", HttpStatus.UNAUTHORIZED),

    // DataBaseException
    TODO_NOT_FOUND("D001", "해당하는 Todo 엔티티는 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    EMAIL_AUTH_INFO_NOT_FOUND("D002", "해당하는 EmailAuthInfo 엔티티는 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("D003", "해당하는 User 엔티티는 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    // CommonException
    UNEXPECTED_EXCEPTION("C001", "알 수 없는 예외입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
