package com.example.brandbeacon.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProjectDetailResponse {
    private Long projectId;           // 프로젝트 고유 번호
    private String projectName;       // 프로젝트 이름
    private String brandIntro;        // 브랜드 소개
    private String referenceType;     // 레퍼런스 희망 항목
    private List<String> keywords;    // 선택했던 키워드 이름들
    private List<String> imgUrls;     // 등록했던 이미지 주소들
    private LocalDateTime createdAt;  // 생성 일자
}