package com.ssafy.apt.service;

import com.ssafy.apt.dto.HouseInfoDto;
import com.ssafy.apt.vo.DongCode;
import com.ssafy.apt.vo.HouseDeals;
import com.ssafy.apt.vo.HouseInfos;

import java.util.List;
import java.util.Map;

public interface AptService {
    // 특정 지역(동 코드)에 해당하는 아파트 정보 조회
    List<HouseInfos> getAptInfoByArea(String dongCode);
    List<HouseInfoDto> getAptInfoByAreaWithDeals(String dongCode);
    List<HouseInfos> getAptInfoByName(String keyword);
    List<HouseInfoDto> getAptInfoByNameWithDeals(String keyword);

    // 특정 아파트 이름으로 아파트 거래 정보 조회
    List<HouseDeals> getAptDealByAptNM(String aptSeq);

    // 동코드 검색
    DongCode findDongCodeByName(String dongName);
    DongCode getDongCodeByDetails(String sidoName, String gugunName, String dongName);

    // 트라이 검색
    List<String> searchApt(String keyword);
    List<String> searchAptNm(String keyword);
    String findRoute(String userLatitude, String userLongitude, String destLatitude, String destLongitude) throws Exception;
    String getCoordinates(String query) throws Exception;
}
