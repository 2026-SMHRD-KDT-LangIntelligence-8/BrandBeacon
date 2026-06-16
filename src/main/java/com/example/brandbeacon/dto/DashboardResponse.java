package com.example.brandbeacon.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    // 1. 헤더 타이틀용
    private String projectName;

    // 2. 브랜드 방향 키워드
    private List<String> keywords;

    // 3. 브랜드 일관성 점수 (예: 87)
    private Integer consistencyScore;

    // 4. 포지셔닝 맵용 내 브랜드 좌표 (프론트의 left, top 퍼센트 값)
    private Float positionX; // left 값 (예: 65.0)
    private Float positionY; // top 값 (예: 50.0)

    // 5. 맵에 뿌려질 기성 브랜드 리스트
    private List<ReferenceBrandDto> referenceBrands;

    // 6. 인사이트 리스트
    private List<String> insights;

    // 7. 유사 브랜드 Top3
    private List<SimilarBrandDto> similarBrands;

    @Getter
    @Builder
    public static class SimilarBrandDto {
        private String brandName;
        private String category;
    }

    // 기성 브랜드 정보
    @Getter
    @Builder
    public static class ReferenceBrandDto {
        private String brandName;
        private Float brandX;
        private Float brandY;
    }


}
