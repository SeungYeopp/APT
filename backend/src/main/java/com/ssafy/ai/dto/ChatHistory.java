package com.ssafy.ai.dto;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ChatHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String role;

    @Column(columnDefinition = "TEXT")
    private String content;
}