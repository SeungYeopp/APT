package com.ssafy.interest.service;

import com.ssafy.apt.vo.DongCode;
import com.ssafy.apt.vo.HouseInfos;
import com.ssafy.interest.dto.BookmarkStatusDto;
import com.ssafy.interest.mapper.InterestMapper;
import com.ssafy.interest.dto.InterestAreaDto;
import com.ssafy.interest.dto.InterestHouseDto;
import com.ssafy.interest.vo.InterestHouse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterestServiceImpl implements InterestService {

    private final InterestMapper interestMapper;

    @Override
    public List<InterestHouseDto> getRecentlyViewedApartments(String userId) {
        return interestMapper.findRecentlyViewedApartments(userId);
    }

    @Override
    public List<InterestHouseDto> getBookmarkedApartments(String userId) {
        return interestMapper.findBookmarkedApartments(userId);
    }

    @Override
    public void addViewedApartment(InterestHouseDto interestHouseDto) {
        interestMapper.insertViewedApartment(interestHouseDto);
    }

    @Override
    public List<InterestAreaDto> getRecentlyViewedNeighborhoods(String userId) {
        return interestMapper.findRecentlyViewedNeighborhoods(userId);
    }

    @Override
    public List<InterestAreaDto> getFavoriteNeighborhoods(Long userId) {
        return interestMapper.findFavoriteNeighborhoods(userId);
    }

    @Override
    public void addViewedNeighborhood(InterestAreaDto interestAreaDto) {
        interestMapper.insertViewedNeighborhood(interestAreaDto);
    }

    @Override
    public void syncLocalStorageToDb(List<InterestHouseDto> apartments) {
        if (apartments != null && !apartments.isEmpty()) {
            apartments.forEach(apartment -> {
                System.out.println("Processing apartment: " + apartment); // 로그 추가
                interestMapper.insertViewedApartment(apartment);
            });
        } else {
            System.out.println("No apartments to sync."); // 로그 추가
        }
    }

    @Override
    public void syncLocalStorageToDbNh(List<InterestAreaDto> neighborhoods) {
        if (neighborhoods != null && !neighborhoods.isEmpty()) {
            neighborhoods.forEach(neighborhood -> {
                System.out.println("Processing neighborhoods: " + neighborhood); // 로그 추가
                interestMapper.insertViewedNeighborhood(neighborhood);
            });
        } else {
            System.out.println("No neighborhoods to sync."); // 로그 추가
        }
    }


    @Override
    public HouseInfos getApartmentDetails(String houseInfoId) {
        return interestMapper.findApartmentDetailsByHouseInfoId(houseInfoId);
    }

    @Override
    public DongCode getNeighborhoodDetails(String dongCd) {
        return interestMapper.findNeighborhoodDetailsByDongCd(dongCd);
    }

    @Override
    public boolean existsApartment(String houseInfoId, int userId) {
        return interestMapper.existsApartment(houseInfoId, userId) > 0;
    }

    @Override
    public boolean existsNeighborhood(String dongCd, int userId) {
        return interestMapper.existsNeighborhood(dongCd, userId) > 0;
    }

    public boolean getBookmarkStatus(String houseInfoId, Long userId) {
        Boolean result = interestMapper.selectBookmarkStatus(houseInfoId, userId);
        return result != null && result; // null일 경우 false 반환
    }

    public boolean getBookmarkStatusNh(String dongCd, Long userId) {
        Boolean result = interestMapper.selectBookmarkStatusNh(dongCd, userId);
        return result != null && result; // null일 경우 false 반환
    }

    @Override
    public List<Map<String, Object>> getTopViewedApartments() {
        return interestMapper.findTopViewedApartments();
    }

    @Override
    public void deleteApartment(String houseInfoId, Long userId) {
        interestMapper.deleteApartment(houseInfoId, userId);
    }

    @Override
    public void deleteNeighborhood(String dongCd, Long userId) {
        interestMapper.deleteNeighborhood(dongCd, userId);
    }
}
