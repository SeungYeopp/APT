package com.ssafy.interest.dto;

import com.ssafy.user.vo.User;
import lombok.Data;

@Data
public class InterestAreaDto {
    private Long interestId;       // 관심 목록 ID
    private String userId;         // User ID
    private String dongCd;         // 동 코드
    private String sidoName;       // 시도 이름
    private String gugunName;      // 구군 이름
    private String dongName;       // 동 이름
    private boolean bookmarked;    // 북마크 여부
    private String time;           // 조회 시간
}
