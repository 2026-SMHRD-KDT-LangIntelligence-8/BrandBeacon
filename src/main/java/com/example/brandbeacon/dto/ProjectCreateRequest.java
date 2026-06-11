package com.example.brandbeacon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProjectCreateRequest {

    private String projectName;    // 프로젝트 이름
    private String brandIntro;     // 브랜드 소개
    private String referenceType;   // 레퍼런스 희망 항목
    private List<String> keywordIds; // 선택한 무드 키워드 ID 목록
    private List<String> keywords;
    private List<String> imgUrls;  // 사용자가 등록한 무드보드 이미지 주소 목록
}
