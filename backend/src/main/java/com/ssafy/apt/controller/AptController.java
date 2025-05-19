package com.ssafy.apt.controller;

import com.ssafy.apt.dto.HouseInfoDto;
import com.ssafy.apt.service.AptService;
import com.ssafy.apt.vo.DongCode;
import com.ssafy.apt.vo.HouseDeals;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
//@CrossOrigin("*")
@RequestMapping("/apt")
@RequiredArgsConstructor
@Tag(name = "Apt 컨트롤러", description = "아파트 정보 및 실거래가 정보 관리하는 클래스")
public class AptController {

    private final AptService service;

    @Operation(summary = "동별 아파트 검색", description = "지역별 아파트 정보를 조회할 수 있습니다.")
    @Parameter(name = "location", description = "시/시구군/시구군동 코드", required = true, in = ParameterIn.PATH, example = "서울시 종로구 1111010200")
    @GetMapping("/area/{location}")
    public List<HouseInfoDto> getAptInfoByArea(@PathVariable("location") String location) {

        return service.getAptInfoByAreaWithDeals(location);
    }

    @Operation(summary = "키워드로 아파트 검색", description = "키워드에 일치하는 아파트를 조회할 수 있습니다.")
    @Parameter(name = "keyword", description = "검색어", required = true, in = ParameterIn.PATH, example = "현대")
    @GetMapping("/name/{keyword}")
//    public List<HouseInfos> getAptInfoByName(@PathVariable("keyword") String keyword) {
//        return service.getAptInfoByName(keyword);
//    }
    public List<HouseInfoDto> getAptInfoByName(@PathVariable("keyword") String keyword) {
        return service.getAptInfoByNameWithDeals(keyword);
    }

    @Operation(summary = "키워드로 동 검색", description = "키워드에 일치하는 동코드를 조회할 수 있습니다.")
    @Parameter(name = "keyword", description = "검색어", required = true, in = ParameterIn.PATH, example = "역삼동")
    @GetMapping("/dong/{keyword}")
    public DongCode getDongCodeByName(@PathVariable("keyword") String keyword) {
        return service.findDongCodeByName(keyword);
    }

    @GetMapping("/code")
    public ResponseEntity<DongCode> getDongCodeByDetails(
            @RequestParam String sidoName,
            @RequestParam String gugunName,
            @RequestParam String dongName) {
        DongCode dongCode = service.getDongCodeByDetails(sidoName, gugunName, dongName);

        if (dongCode != null) {
            return ResponseEntity.ok(dongCode);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "아파트별 실거래가 검색", description = "아파트별 실거래가를 조회할 수 있습니다.")
    @Parameter(name = "aptSeq", description = "아파트 ID", required = true, in = ParameterIn.PATH, example = "11110-102")
    @GetMapping("/housedeal/{aptSeq}")
    public List<HouseDeals> getAptDealByAptNM(@PathVariable("aptSeq") String aptSeq) {
        List<HouseDeals> list = service.getAptDealByAptNM(aptSeq);
        list.sort((o1, o2) -> {
            if (Objects.equals(o2.getDealYear(), o1.getDealYear())) {
                if (Objects.equals(o2.getDealMonth(), o1.getDealMonth())) {
                    return o2.getDealDay() - o1.getDealDay();
                }
                return o2.getDealMonth() - o1.getDealMonth();
            }
            return o2.getDealYear() - o1.getDealYear();
        });
        return list;
    }

    // localhost:8080/apt/searchAjax?keyword=화곡
    @Operation(summary = "트라이 검색", description = "아파트 이름을 트라이로 빠르게 검색합니다.")
    @Parameter(name = "keyword", description = "검색키워드", required = true, example = "화곡")
    @GetMapping("/searchAjax")
    public List<String> searchAjax(@RequestParam String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return List.of(); // return an empty JSON array
        } else {
            // Perform the search using TrieSearch
            List<String> results = service.searchApt(keyword);
//            for(String res : results) {
//                log.info(res);
//            }
//            log.info(""+results.stream()
//                    .map(result -> "\"" + result + "\"")
//                    .toList());

            // Format each result as a JSON-compatible string
            return results.stream()
                    .map(result -> "\"" + result + "\"")
                    .collect(Collectors.toList());
        }
    }

    @Operation(summary = "아파트 이름 트라이 검색", description = "아파트 이름을 트라이로 빠르게 검색합니다.")
    @Parameter(name = "keyword", description = "검색 키워드", required = true, example = "강남")
    @GetMapping("/searchApartmentAjax")
    public List<String> searchApartmentAjax(@RequestParam String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return List.of(); // 빈 JSON 배열 반환
        } else {
            // TrieSearchApt를 이용한 검색 수행
            List<String> results = service.searchAptNm(keyword);

            // JSON 호환 문자열 형식으로 변환하여 반환
            return results;
        }
    }


    @Operation(summary = "길찾기 API", description = "출발지와 목적지 좌표를 입력받아 최적의 경로를 반환합니다.")
    @PostMapping("/findRoute")
    public ResponseEntity<String> findRoute(@RequestParam String userLatitude, @RequestParam String userLongitude,
                                            @RequestParam String destLatitude, @RequestParam String destLongitude) {
        try {
            String routeData = service.findRoute(userLatitude, userLongitude, destLatitude, destLongitude);
            return ResponseEntity.ok(routeData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/getCoordinates")
    public ResponseEntity<String> getCoordinates(@RequestParam String query) {
        try {
            // Kakao Local API를 통해 위경도 가져오기
            String coordinates = service.getCoordinates(query);
            return ResponseEntity.ok(coordinates);
        } catch (Exception e) {
            // 에러 처리
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
