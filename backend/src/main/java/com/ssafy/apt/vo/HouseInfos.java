package com.ssafy.apt.vo;

import com.ssafy.review.vo.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Schema(title = "HouseInfos (아파트 세부정보)", description = "각 아파트의 자세한 설명")
public class HouseInfos {
    @Id
    @Schema(description = "아파트 고유 ID", example = "11110-102")
    private String aptSeq; // 아파트 고유 ID

    @Schema(description = "아파트 이름", example = "현대뜨레비앙")
    private String aptNm; // 아파트 이름
    @Schema(description = "시군구 코드", example = "11110")
    private String sggCd; // 시군구 코드
    @Schema(description = "읍면동 코드", example = "13300")
    private String umdCd; // 읍면동 코드
    @Schema(description = "행정구역명", example = "익선동")
    private String umdNm; // 행정구역명
    @Schema(description = "도로명 주소", example = "돈화문로11가길")
    private String roadNm; // 도로명 주소
    @Schema(description = "지번 주소", example = "55")
    private String jibun; // 지번 주소
    @Schema(description = "건축 연도", example = "2003")
    private Integer buildYear; // 건축 연도
    @Schema(description = "위도", example = "37.5751118972517")
    private Double latitude; // 위도
    @Schema(description = "경도", example = "126.990057597088")
    private Double longitude; // 경도

    @Schema(description = "동 코드=sggCd+umdCd", example = "1111013300")
    private String dongCd; // 동 코드 (sggCd와 umdCd를 결합한 값)

    @OneToMany(mappedBy = "houseInfos", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Schema(description = "이 아파트의 거래 내역")
    private List<HouseDeals> deals; // 아파트의 거래 내역

    @OneToMany(mappedBy = "houseInfos", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Schema(description = "이 아파트의 리뷰")
    private List<Review> reviews; // 아파트에 대한 리뷰 목록

    // 저장 전 dongCd 자동 설정
    @PrePersist
    @PreUpdate
    private void prePersist() {
        this.dongCd = sggCd + umdCd;
    }

    public HouseInfos(String aptSeq) {
        this.aptSeq = aptSeq;
    }
}
