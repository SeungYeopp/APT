package com.ssafy.review.vo;

import com.ssafy.user.vo.User;
import com.ssafy.apt.vo.HouseInfos;
import com.ssafy.util.Timestamped;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Schema(title = "ReviewDto (리뷰 Dto)", description = "리뷰 작성에 필요한 기본 리뷰 Dto")
public class Review extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "리뷰 ID (auto)", example = "1")
    private Long reviewId; // 리뷰 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apt_seq")
    @Schema(description = "리뷰할 아파트")
    private HouseInfos houseInfos; // 아파트 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id")
    @Schema(description = "리뷰하는 사용자")
    private User user; // 리뷰 작성자

    @Schema(description = "리뷰 내용", example = "별로에요")
    private String content; // 리뷰 내용
    @Schema(description = "별점", example = "1")
    private Double rating; // 평점 (1-5)

    @Schema(description = "리뷰 이미지 URL", example = "http://example.com/image.jpg")
    private String imageUrl; // 이미지 URL

    public Review(User user, HouseInfos houseInfos, String content, Double rating, String imageUrl) {
        super();
        this.user = user;
        this.houseInfos = houseInfos;
        this.content = content;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }
}
