package com.ssafy.util;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class Timestamped {

    private LocalDateTime time; // 생성 또는 클릭/검색 시간

    public Timestamped() {
        this.time = LocalDateTime.now();
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}

