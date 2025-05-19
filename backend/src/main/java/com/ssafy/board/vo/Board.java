package com.ssafy.board.vo;

import com.ssafy.board.dto.FileInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Schema(title = "Board : 게시글정보", description = "게시글의 상세 정보를 나타낸다.")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "글번호")
    private int articleNo;
    @Schema(description = "작성자 아이디")
    private String userId;
    @Schema(description = "글제목")
    private String subject;
    @Schema(description = "글내용")
    private String content;
    @Schema(description = "조회수")
    private int hit;
    @Schema(description = "작성일")
    private String registerTime;

}