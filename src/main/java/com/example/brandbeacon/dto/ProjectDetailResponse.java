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
    private Long folderId;
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
    private String moodboardData; // 카테고리별 무드보드 구조 JSON 문자열
    private String imageAlignmentsData; // 이미지별 유사도 배열 JSON 문자열
    private LocalDateTime createdAt;
}