package com.ssafy.apt.vo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class SidoCode {
    @Id
    private String sidoCode;
    private String sidoName;

}
