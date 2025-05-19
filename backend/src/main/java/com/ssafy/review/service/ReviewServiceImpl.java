package com.ssafy.review.service;

import com.ssafy.apt.vo.HouseInfos;
import com.ssafy.review.mapper.ReviewMapper;
import com.ssafy.review.vo.Review;
import com.ssafy.user.vo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;

    @Override
    public void addReview(Review review) {
        reviewMapper.addReview(review);
    }

    @Override
    public List<Review> getReviewsByHouseId(String houseId) {
        return reviewMapper.getReviewsByHouseId(houseId);
    }

    @Override
    public boolean updateReview(Long reviewId, String content, Double rating, String userId, boolean isAdmin, MultipartFile file) throws Exception {
        // 기존 리뷰 가져오기
        Review review = reviewMapper.getReviewById(reviewId);
        if (review == null) {
            throw new RuntimeException("Review not found");
        }

        // 사용자 권한 확인
        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            return false; // 권한 없음
        }

        // 이미지 업로드 처리
        String imageUrl = review.getImageUrl(); // 기존 이미지 URL
        if (file != null && !file.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            Files.createDirectories(Paths.get(uploadDir)); // 업로드 디렉터리 생성
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            imageUrl = "/uploads/" + fileName; // 새로운 이미지 URL 설정
        }

        // 리뷰 업데이트
        review.setContent(content);
        review.setRating(rating);
        review.setImageUrl(imageUrl); // 이미지 URL 업데이트
        reviewMapper.updateReview(review);

        return true;
    }

    @Override
    public boolean deleteReview(Long reviewId, String userId, Boolean isAdmin) {
        Review review = reviewMapper.getReviewById(reviewId);

        if (review == null) return false;

        // 관리자이거나 리뷰 작성자일 경우 삭제 가능
        if (isAdmin || review.getUser().getId().equals(userId)) {
            reviewMapper.deleteReview(reviewId);
            return true;
        }

        return false;
    }

    @Override
    public List<Review> getHighRatingReviews() {
        return reviewMapper.getHighRatingReviews();
    }

    @Override
    public List<Review> getReviewsByUserId(String userId) {
        return reviewMapper.getReviewsByUserId(userId);
    }

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // 폴더 생성
            }
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + fileName; // 반환될 URL
        } catch (Exception e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }

    @Override
    public Review createReview(String userId, String houseId, String content, Double rating, String imageUrl) {
        User user = new User(userId); // User 객체 생성 (id만 필요)
        HouseInfos houseInfos = new HouseInfos(houseId); // HouseInfos 객체 생성 (id만 필요)
        Review review = new Review(user, houseInfos, content, rating, imageUrl);
        reviewMapper.addReview(review);
        return review;
    }

}
