package com.example.brandbeacon.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MoodboardResponse {

    // 1. 프로젝트 기본 정보
    private String projectName;
    private String brandIntro;

    // 2. 키워드와 이미지 목록
    private List<String> keywordNames;
    private List<String> moodboardImgUrls;

    // 3. 내 프로젝트의 포지셔닝 맵 좌표 (X, Y)
    private Float currentX;
    private Float currentY;

    // 4. 비교군(기성 브랜드) 목록
    private List<BrandDto> referenceBrands;

    // 기성 브랜드 정보
    @Getter
    @Builder
    public static class BrandDto {
        private String brandName;
        private Float brandX;
        private Float brandY;
    }
}
