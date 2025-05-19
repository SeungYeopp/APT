package com.ssafy.apt.vo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class GugunCode {
    @Id
    private String gugunCode;
    private String gugunName;
}
