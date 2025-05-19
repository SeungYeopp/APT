package com.ssafy.interest.service;

import com.ssafy.apt.vo.DongCode;
import com.ssafy.apt.vo.HouseInfos;
import com.ssafy.interest.dto.BookmarkStatusDto;
import com.ssafy.interest.dto.InterestAreaDto;
import com.ssafy.interest.dto.InterestHouseDto;

import java.util.List;
import java.util.Map;

public interface InterestService {

    List<InterestHouseDto> getRecentlyViewedApartments(String userId);

    List<InterestHouseDto> getBookmarkedApartments(String userId);

    void addViewedApartment(InterestHouseDto interestHouseDto);

    List<InterestAreaDto> getRecentlyViewedNeighborhoods(String userId);

    List<InterestAreaDto> getFavoriteNeighborhoods(Long userId);

    void addViewedNeighborhood(InterestAreaDto interestAreaDto);

    HouseInfos getApartmentDetails(String aptSeq);
    DongCode getNeighborhoodDetails(String dongCd);
    void syncLocalStorageToDb(List<InterestHouseDto> apartments);
    void syncLocalStorageToDbNh(List<InterestAreaDto> neighborhoods);

    boolean existsApartment(String houseInfoId, int userId);
    boolean existsNeighborhood(String dongCd, int userId);
    boolean getBookmarkStatus(String houseInfoId, Long userId);
    boolean getBookmarkStatusNh(String dongCd, Long userId);

    List<Map<String, Object>> getTopViewedApartments();

    void deleteApartment(String houseInfoId, Long userId);

    void deleteNeighborhood(String dongCd, Long userId);

}
