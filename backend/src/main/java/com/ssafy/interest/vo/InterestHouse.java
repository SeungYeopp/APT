package com.ssafy.interest.vo;

import com.ssafy.apt.vo.HouseInfos;
import com.ssafy.user.vo.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "unique_interest_house",
                columnNames = {"user_id", "house_info_id"}
        )
)
public class InterestHouse extends Interest {

    @ManyToOne
    @JoinColumn(name = "house_info_id", nullable = false) // house_info 테이블의 apt_seq 열과 조인
    private HouseInfos houseInfo; // 관심 아파트 정보

    public InterestHouse(User user, HouseInfos houseInfo, boolean bookmarked) {
        super(user, bookmarked);
        this.houseInfo = houseInfo;
    }
}
