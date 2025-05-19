package com.ssafy.interest.mapper;

import com.ssafy.apt.vo.DongCode;
import com.ssafy.apt.vo.HouseInfos;
import com.ssafy.interest.dto.InterestAreaDto;
import com.ssafy.interest.dto.InterestHouseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface InterestMapper {

    List<InterestHouseDto> findRecentlyViewedApartments(String userId);

    List<InterestHouseDto> findBookmarkedApartments(String userId);

    void insertViewedApartment(InterestHouseDto interestHouseDto);

    List<InterestAreaDto> findRecentlyViewedNeighborhoods(String userId);

    List<InterestAreaDto> findFavoriteNeighborhoods(Long userId);

    void insertViewedNeighborhood(InterestAreaDto interestAreaDto);

    HouseInfos findApartmentDetailsByHouseInfoId(String houseInfoId);
    DongCode findNeighborhoodDetailsByDongCd(String dongCd);

    int existsApartment(@Param("houseInfoId") String houseInfoId, @Param("userId") int userId);
    int existsNeighborhood(@Param("dongCd") String dongCd, @Param("userId") int userId);

    Boolean selectBookmarkStatus(@Param("houseInfoId") String houseInfoId, @Param("userId") Long userId);
    Boolean selectBookmarkStatusNh(@Param("dongCd") String dongCd, @Param("userId") Long userId);

    List<Map<String, Object>> findTopViewedApartments();

    void deleteApartment(@Param("houseInfoId") String houseInfoId, @Param("userId") Long userId);

    void deleteNeighborhood(@Param("dongCd") String dongCd, @Param("userId") Long userId);


}
