package com.ssafy.review.service;

import com.ssafy.review.vo.Review;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {
    void addReview(Review review);
    List<Review> getReviewsByHouseId(String houseId);
    boolean updateReview(Long reviewId, String content, Double rating, String userId, boolean isAdmin, MultipartFile file) throws Exception;
    boolean deleteReview(Long reviewId, String userId, Boolean isAdmin);
    List<Review> getHighRatingReviews();
    List<Review> getReviewsByUserId(String userId);
    String uploadImage(MultipartFile file); // 이미지 업로드 메서드
    Review createReview(String userId, String houseId, String content, Double rating, String imageUrl);
}
