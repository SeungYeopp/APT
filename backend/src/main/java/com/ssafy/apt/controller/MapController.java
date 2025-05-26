package com.ssafy.apt.controller;

import java.util.List;

import com.ssafy.apt.service.MapService;
import com.ssafy.apt.dto.SidoGugunCodeDto;
import com.ssafy.apt.vo.DongCode;
import com.ssafy.apt.vo.GugunCode;
import com.ssafy.apt.vo.SidoCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/map")
@Tag(name = "시도 구군 컨트롤러", description = "시도 구군정보를 처리하는 클래스.")
@Slf4j
public class MapController {

    private final MapService mapService;

    public MapController(MapService mapService) {
        super();
        this.mapService = mapService;
    }

    @Operation(summary = "시도 정보", description = "전국의 시도를 반환한다.")
    @GetMapping("/sido")
    public ResponseEntity<List<SidoCode>> sido() throws Exception {
        //log.info("sido - 호출");
        List<SidoCode> sido = mapService.getSido();
        log.info(sido.toString());
        return new ResponseEntity<List<SidoCode>>(sido, HttpStatus.OK);
    }

    @Operation(summary = "구군 정보", description = "시도에 속한 구군을 반환한다.")
    @GetMapping("/gugun")
    public ResponseEntity<List<GugunCode>> gugun(
            @RequestParam("sido") @Parameter(description = "시도코드.", required = true) String sido) throws Exception {
        //log.info("gugun - 호출");
        List<GugunCode> gugun = mapService.getGugunInSido(sido);
        log.info(gugun.toString());
        return new ResponseEntity<List<GugunCode>>(gugun, HttpStatus.OK);
    }

    @Operation(summary = "동 정보", description = "구군에 속한 동을 반환한다.")
    @GetMapping("/dong")
    public ResponseEntity<List<DongCode>> dong(
            @RequestParam("gugun") @Parameter(description = "구군코드.", required = true) String gugun) throws Exception {
        //log.info("dong - 호출");
        List<DongCode> dong = mapService.getDongInGugun(gugun);
        log.info(dong.toString());
        return new ResponseEntity<List<DongCode>>(dong, HttpStatus.OK);
    }

}
