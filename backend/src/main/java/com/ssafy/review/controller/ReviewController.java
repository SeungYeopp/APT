package com.ssafy.review.controller;

import com.ssafy.review.service.ReviewService;
import com.ssafy.review.vo.Review;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
@Tag(name = "리뷰 컨트롤러", description = "사용자의 아파트 리뷰를 관리합니다.")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성 API", description = "리뷰를 추가할 수 있습니다.")
    @PostMapping("/add")
    public ResponseEntity<?> addReview(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("content") String content,
            @RequestParam("rating") Double rating,
            @RequestParam("userId") String userId,
            @RequestParam("houseId") String houseId) {
        try {
            String imageUrl = null;

            // 파일이 존재하면 업로드 처리
            if (file != null && !file.isEmpty()) {
                imageUrl = reviewService.uploadImage(file);
            }

            // 리뷰 생성
            Review review = reviewService.createReview(userId, houseId, content, rating, imageUrl);
            return ResponseEntity.ok().body("Review added successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to add review: " + e.getMessage());
        }
    }


    // 특정 아파트에 대한 리뷰 목록 조회 API
    @Operation(summary = "특정 아파트에 대한 리뷰 목록 조회 API", description = "원하는 아파트의 리뷰를 조회합니다.")
    @Parameter(name = "aptSeq", description = "아파트 ID", required = true, in = ParameterIn.PATH, example = "11110-102")
    @GetMapping("/{aptSeq}")
    public ResponseEntity<List<Review>> getReviewsByHouseId(@PathVariable String aptSeq) {
        List<Review> reviews = reviewService.getReviewsByHouseId(aptSeq);
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 수정 API
    @Operation(summary = "리뷰 수정 API", description = "리뷰를 수정합니다.")
    @Parameters({
            @Parameter(name = "reviewId", description = "리뷰 ID", required = true, in = ParameterIn.PATH, example = "6"),
            @Parameter(name = "userId", description = "사용자 ID", required = true, in = ParameterIn.QUERY, example = "0ys"),
            @Parameter(name = "isAdmin", description = "관리자 여부", required = true, in = ParameterIn.QUERY, example = "1")
    })
    @PutMapping("/update/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestParam("userId") String userId,
            @RequestParam("isAdmin") boolean isAdmin,
            @RequestParam("content") String content,
            @RequestParam("rating") Double rating,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        System.out.println("Request received: userId=" + userId + ", isAdmin=" + isAdmin);
        try {
            // 서비스 레이어에서 업데이트 처리
            boolean updated = reviewService.updateReview(reviewId, content, rating, userId, isAdmin, file);
            if (updated) {
                return ResponseEntity.ok().body("Review updated successfully!");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Permission denied.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // 리뷰 삭제 API
    @Operation(summary = "리뷰 삭제 API", description = "작성한 사용자 또는 관리자만 리뷰를 삭제할 수 있습니다.")
    @Parameters({
            @Parameter(name = "reviewId", description = "리뷰 ID", required = true, in = ParameterIn.PATH, example = "6"),
            @Parameter(name = "userId", description = "사용자 ID", required = true, example = "0ys"),
            @Parameter(name = "isAdmin", description = "관리자 여부", required = true, example = "false")
    })
    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam String userId,
            @RequestParam Boolean isAdmin
    ) {
        boolean deleted = reviewService.deleteReview(reviewId, userId, isAdmin);
        if (deleted) {
            return ResponseEntity.ok().body("Review deleted successfully!");
        }
        return ResponseEntity.status(403).body("Permission denied: Unable to delete review.");
    }

    // Add logger to the controller
    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    // Get high-rating reviews with logging
    @Operation(summary = "평점 높은 리뷰 목록 조회 API", description = "평점 4 이상인 리뷰를 조회합니다.")
    @GetMapping("/high-rating")
    public ResponseEntity<List<Review>> getHighRatingReviews() {
        List<Review> highRatingReviews = reviewService.getHighRatingReviews();

        // Log the reviews
        if (highRatingReviews != null && !highRatingReviews.isEmpty()) {
//            logger.info("Fetched High-Rating Reviews:");
//            highRatingReviews.forEach(review -> {
//                logger.info("Review ID: {}, Content: {}, Rating: {}, User: {}, Time: {}",
//                        review.getReviewId(),
//                        review.getContent(),
//                        review.getRating(),
//                        review.getUser().getNickname(),
//                        review.getTime());
//            });
        } else {
            logger.info("No high-rating reviews found.");
        }

        return ResponseEntity.ok(highRatingReviews);
    }

    @Operation(summary = "Fetch reviews by user ID", description = "Fetch all reviews written by a specific user.")
    @Parameter(name = "userId", description = "User ID", required = true, in = ParameterIn.PATH, example = "user123")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUserId(@PathVariable String userId) {
        List<Review> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }


}
