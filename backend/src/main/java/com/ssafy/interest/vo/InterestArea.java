package com.ssafy.interest.vo;

import com.ssafy.user.vo.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "unique_interest_area",
                columnNames = {"user_id", "dongCd"}
        )
)
public class InterestArea extends Interest {

    @Column(nullable = false)
    private String dongCd; // 관심있는 동네 코드

    public InterestArea(User user, String dongCd, boolean bookmarked) {
        super(user, bookmarked);
        this.dongCd = dongCd;
    }
}
