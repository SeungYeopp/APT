package com.ssafy.apt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Schema(title = "DongCode (지역 동코드)", description = "지역(동네)별 코드를 관리합니다.")
public class DongCode {
    @Id
    @Schema(description = "동코드", example = "1111010200")
    private String dongCd;
    @Schema(description = "시", example = "서울특별시")
    private String sidoName;
    @Schema(description = "구", example = "종로구")
    private String gugunName;
    @Schema(description = "동", example = "신교동")
    private String dongName;
}
