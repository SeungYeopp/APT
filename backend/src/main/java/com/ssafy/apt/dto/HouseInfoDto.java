package com.ssafy.apt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Schema(title = "아파트 정보", description = "검색시 필요한 아파트 정보, 최근 거래 내역을 같이 불러온다.")
public class HouseInfoDto {

    // house_infos 필드
    private String aptSeq;              // 아파트 식별자
    private String aptNm;             // 아파트 이름
    private Integer buildYear;        // 건축 연도
    private String dongCd;            // 동 코드
    private String jibun;             // 지번
    private Double latitude;          // 위도
    private Double longitude;         // 경도
    private String roadNm;            // 도로명
    private String sggCd;             // 시군구 코드
    private String umdCd;             // 읍면동 코드
    private String umdNm;             // 읍면동 이름

    // recent_deals 필드
    private String recentDealAmount; // 최근 거래 금액
    private String recentDealDate;    // 최근 거래 날짜 (YYYY-MM-DD)
    private String exclusiveArea; // 평수 (전용면적)
    private String recentFloor;      // 최근 거래 층수
}
