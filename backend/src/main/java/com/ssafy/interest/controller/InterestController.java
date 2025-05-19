package com.ssafy.interest.controller;

import com.ssafy.apt.vo.DongCode;
import com.ssafy.apt.vo.HouseInfos;
import com.ssafy.interest.dto.BookmarkStatusDto;
import com.ssafy.interest.service.InterestService;
import com.ssafy.interest.dto.InterestAreaDto;
import com.ssafy.interest.dto.InterestHouseDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/interest")
@RequiredArgsConstructor
@Slf4j
public class InterestController {

    private final InterestService interestService;

    // 최근 본 아파트 (세션 또는 DB에서 가져옴)
    @GetMapping("/recent-apartments")
    public List<InterestHouseDto> getRecentlyViewedApartments(@RequestParam(required = false) String userId) {
        if (userId == null) {
            // 로그인하지 않은 경우, DB에서 조회하지 않음
            return List.of();
        }
        return interestService.getRecentlyViewedApartments(userId);
    }

    @PostMapping("/sync-apartments")
    public void syncLocalStorageToDb(@RequestBody List<InterestHouseDto> apartments) {
        System.out.println("Received apartments data: " + apartments); // 로그 추가
        interestService.syncLocalStorageToDb(apartments);
    }

    @PostMapping("/sync-neighborhoods")
    public void syncLocalStorageToDbNh(@RequestBody List<InterestAreaDto> neighborhoods) {
        System.out.println("Received neighborhoods data: " + neighborhoods); // 로그 추가
        interestService.syncLocalStorageToDbNh(neighborhoods);
    }

    // 북마크된 아파트
    @GetMapping("/bookmarked-apartments")
    public List<InterestHouseDto> getBookmarkedApartments(@RequestParam String userId) {
        return interestService.getBookmarkedApartments(userId);
    }

    // 최근 본 동네 (세션 또는 DB에서 가져옴)
    @GetMapping("/recent-neighborhoods")
    public List<InterestAreaDto> getRecentlyViewedNeighborhoods(@RequestParam(required = false) String userId) {
        if (userId == null) { // 비로그인 시 세션 데이터 반환
            return List.of();
        }
        return interestService.getRecentlyViewedNeighborhoods(userId); // 로그인 시 DB 데이터 반환
    }

    // 즐겨찾는 동네
    @GetMapping("/favorite-neighborhoods")
    public List<InterestAreaDto> getFavoriteNeighborhoods(@RequestParam Long userId) {
        return interestService.getFavoriteNeighborhoods(userId);
    }

    // 아파트 열람 추가 (세션 또는 DB 저장)
    @PostMapping("/view-apartment")
    public void addViewedApartment(@RequestBody InterestHouseDto interestHouseDto, HttpSession session) {
        if (interestHouseDto.getUserId() == null) { // 비로그인 시 세션에 저장
            List<InterestHouseDto> sessionData = (List<InterestHouseDto>) session.getAttribute("recentApartments");
            if (sessionData == null) sessionData = new ArrayList<>();
            sessionData.add(interestHouseDto);
            session.setAttribute("recentApartments", sessionData);
        } else { // 로그인 시 DB 저장
            interestService.addViewedApartment(interestHouseDto);
        }
    }

    // 동네 열람 추가 (세션 또는 DB 저장)
    @PostMapping("/view-neighborhood")
    public void addViewedNeighborhood(@RequestBody InterestAreaDto interestAreaDto, HttpSession session) {
        //System.out.println(interestAreaDto.getUserId());
        if (interestAreaDto.getUserId() == null) { // 비로그인 시 세션에 저장
            List<InterestAreaDto> sessionData = (List<InterestAreaDto>) session.getAttribute("recentNeighborhoods");
            if (sessionData == null) sessionData = new ArrayList<>();
            sessionData.add(interestAreaDto);
            session.setAttribute("recentNeighborhoods", sessionData);
        } else { // 로그인 시 DB 저장
            interestService.addViewedNeighborhood(interestAreaDto);
        }
    }



    @GetMapping("/apartment-details/{houseInfoId}")
    public ResponseEntity<HouseInfos> getApartmentDetails(@PathVariable String houseInfoId) {
        HouseInfos houseInfos = interestService.getApartmentDetails(houseInfoId);
        if (houseInfos != null) {
            return ResponseEntity.ok(houseInfos);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/neighborhood-details/{dongCd}")
    public ResponseEntity<DongCode> getNeighborhoodDetails(@PathVariable String dongCd) {
        DongCode dongCode = interestService.getNeighborhoodDetails(dongCd);
        if (dongCode != null) {
            return ResponseEntity.ok(dongCode);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // houseInfoId 존재 여부 확인
    @PostMapping("/check-apartment")
    public ResponseEntity<Map<String, Boolean>> checkApartment(@RequestBody Map<String, Object> request) {
        String houseInfoId = (String) request.get("houseInfoId");
        int userId = (int) request.get("userId");

        boolean exists = interestService.existsApartment(houseInfoId, userId);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    @GetMapping("/bookmark")
    public ResponseEntity<BookmarkStatusDto> getBookmarkStatus(
            @RequestParam String houseInfoId,
            @RequestParam Long userId) {
        boolean isBookmarked = interestService.getBookmarkStatus(houseInfoId, userId);
        return ResponseEntity.ok(new BookmarkStatusDto(isBookmarked));
    }

    @PostMapping("/check-neighborhood")
    public ResponseEntity<Map<String, Boolean>> checkNeighborhood(@RequestBody Map<String, Object> request) {
        String dongCd = (String) request.get("dongCd");
        int userId = (int) request.get("userId");

        boolean exists = interestService.existsNeighborhood(dongCd, userId);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    @GetMapping("/bookmarkNh")
    public ResponseEntity<BookmarkStatusDto> getBookmarkStatusNh(
            @RequestParam String dongCd,
            @RequestParam Long userId) {
        boolean isBookmarked = interestService.getBookmarkStatusNh(dongCd, userId);
        return ResponseEntity.ok(new BookmarkStatusDto(isBookmarked));
    }

    @GetMapping("/top-viewed-apartments")
    public ResponseEntity<List<Map<String, Object>>> getTopViewedApartments() {
        List<Map<String, Object>> topViewedApartments = interestService.getTopViewedApartments();
        return ResponseEntity.ok(topViewedApartments);
    }

    // 아파트 삭제
    @DeleteMapping("/delete-apartment")
    public ResponseEntity<Void> deleteApartment(@RequestParam String houseInfoId, @RequestParam Long userId) {
        interestService.deleteApartment(houseInfoId, userId);
        return ResponseEntity.noContent().build();
    }

    // 동네 삭제
    @DeleteMapping("/delete-neighborhood")
    public ResponseEntity<Void> deleteNeighborhood(@RequestParam String dongCd, @RequestParam Long userId) {
        interestService.deleteNeighborhood(dongCd, userId);
        return ResponseEntity.noContent().build();
    }


}
