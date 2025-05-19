package com.ssafy.review.mapper;

import com.ssafy.review.vo.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper {
    void addReview(Review review);
    List<Review> getReviewsByHouseId(@Param("houseId") String houseId);
    void updateReview(Review review);
    void deleteReview(@Param("reviewId") Long reviewId);

    // 특정 리뷰 조회 메서드 추가
    Review getReviewById(@Param("reviewId") Long reviewId);

    // rating > 4 리뷰 목록 조회 메서드
    List<Review> getHighRatingReviews();
    List<Review> getReviewsByUserId(@Param("userId") String userId);
}
