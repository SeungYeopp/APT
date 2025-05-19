package com.ssafy.interest.dto;
import com.ssafy.apt.vo.HouseInfos;
import com.ssafy.user.vo.User;
import lombok.Data;

@Data
public class InterestHouseDto {
    private Long interestId;
    private String userId;         // User ID
    private String houseInfoId;    // apt_seq
    private boolean bookmarked;
    private String time;           // 조회 시간
    private HouseInfos houseInfos; // HouseInfos 전체 정보      // 조회 시간
}