package com.example.brandbeacon.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProjectDetailResponse {
    private Long projectId;
    private String projectName;
    private String brandIntro;
    private String referenceType;
    private String brandProfile;
    private String analysisInsight;    // AI 인사이트 JSON 문자열
    private BigDecimal similarityScore; // 브랜드 일관성 점수
    private Float positionX;           // 포지셔닝 맵 X 좌표
    private Float positionY;           // 포지셔닝 맵 Y 좌표
    private List<String> keywords;
    private List<String> imgUrls;
    private LocalDateTime createdAt;
}