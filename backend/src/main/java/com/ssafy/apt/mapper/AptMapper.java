package com.ssafy.apt.mapper;

import com.ssafy.apt.dto.HouseInfoDto;
import com.ssafy.apt.vo.DongCode;
import com.ssafy.apt.vo.HouseDeals;
import com.ssafy.apt.vo.HouseInfos;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface AptMapper {
    List<HouseInfos> getAptInfoByArea(String dong_code); // 지역별 아파트 정보
    List<HouseInfos> getAptInfoByName(String keyword);
    List<HouseDeals> getAptDealByAptNM(String apt_seq); // 아파트별 실거래가

    // 최근 거래 내역 포함 조회
    List<HouseInfoDto> getAptInfoByAreaWithDeals(String dongCode);
    List<HouseInfoDto> getAptInfoByNameWithDeals(String aptName);
    // 모든 HouseInfos와 최근 거래 내역 조회
    List<HouseInfoDto> getAllHouseInfosWithDeals();

    List<DongCode> findAllDongCodes();
    List<String> getAllApartmentNames();
    DongCode findDongCodeByName(String dongName);
    DongCode findDongCodeByDetails(Map<String, String> params);

    // 최근 거래 내역 포함 조회 : 시 구군 코드 (5자: sido_cd + sgg_cd)
    List<HouseInfoDto> getAptInfoByAreaWithDealsBySggAndSido(String location);

    // 최근 거래 내역 포함 조회 : 시 코드 (2자)
    List<HouseInfoDto> getAptInfoByAreaWithDealsBySido(String sidoCd);


}
