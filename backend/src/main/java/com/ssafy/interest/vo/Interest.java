package com.ssafy.interest.vo;

import com.ssafy.user.vo.User;
import com.ssafy.util.Timestamped;
import jakarta.persistence.*;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class Interest extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interestId; // 관심 목록 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 사용자 정보와 매핑

    private boolean bookmarked; // 북마크 여부

    public Interest(User user, boolean bookmarked) {
        super();
        this.user = user;
        this.bookmarked = bookmarked;
    }

    public Interest() {
        super();
    }
}

