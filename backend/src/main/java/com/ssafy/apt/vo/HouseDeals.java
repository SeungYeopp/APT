package com.ssafy.apt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Schema(title = "HouseDeals (아파트 거래내역)", description = "각 아파트별 실거래 내역")
public class HouseDeals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "거래 내역 ID", example = "1")
    private Long dealId; // 거래 내역 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apt_seq")
    @Schema(description = "거래가 발생한 아파트")
    private HouseInfos houseInfos; // 거래가 발생한 아파트

    private String aptDong;
    @Schema(description = "거래 연도", example = "2011")
    private Integer dealYear; // 거래 연도
    @Schema(description = "거래 월", example = "11")
    private Integer dealMonth; // 거래 월
    @Schema(description = "거래 일자", example = "26")
    private Integer dealDay; // 거래 일자
    @Schema(description = "거래 금액", example = "38000")
    private String dealAmount; // 거래 금액
    @Schema(description = "전용 면적", example = "59.92")
    private Double excluUseAr; // 전용 면적
    @Schema(description = "층수", example = "9")
    private String floor; // 층수
}
