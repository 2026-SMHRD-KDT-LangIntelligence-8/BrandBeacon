package com.example.brandbeacon.service;

import com.example.brandbeacon.domain.Keyword;
import com.example.brandbeacon.domain.PositioningMap;
import com.example.brandbeacon.domain.Project;
import com.example.brandbeacon.dto.DashboardResponse;
import com.example.brandbeacon.repository.PositioningMapRepository;
import com.example.brandbeacon.repository.ProjectRepository;
import com.example.brandbeacon.repository.ReferenceBrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final PositioningMapRepository positioningMapRepository;
    private final ReferenceBrandRepository referenceBrandRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getProjectDashboard(Long projectId, Long userId) {

        // 1. 프로젝트 찾기 및 권한 검사
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        if (!project.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 생성한 프로젝트의 분석 결과만 열람할 수 있습니다.");
        }

        // 2. 키워드 리스트 추출
        List<String> keywords = project.getKeywords().stream()
                .map(Keyword::getKeywordName)
                .collect(Collectors.toList());

        // 3. 포지셔닝 맵 좌표
        // AI 연동 전이면 임시로 정중앙(50, 50) 배치
        PositioningMap map = positioningMapRepository.findById(projectId).orElse(null);
        Float currentX = (map != null) ? map.getCurrentX() : 50.0f;
        Float currentY = (map != null) ? map.getCurrentY() : 50.0f;

        // 4. 지도에 깔아둘 기성 브랜드 좌표 리스트 가져오기
        List<DashboardResponse.ReferenceBrandDto> refBrands = referenceBrandRepository.findAll().stream()
                .map(brand -> DashboardResponse.ReferenceBrandDto.builder()
                        .brandName(brand.getBrandName())
                        .brandX(brand.getBrandX())
                        .brandY(brand.getBrandY())
                        .build())
                .collect(Collectors.toList());

        // 5. 인사이트 문구 세팅
        // (추후 파이썬 서버에서 생성된 텍스트로 대체될 예정입니다!)
        List<String> mockInsights = List.of(
                "현재 도출된 가치 벡터는 타겟 시장의 핵심 트렌드 세그먼트에 근접하게 위치하고 있습니다."
        );

        // 6. 프론트로 보낼 수 있도록 반환
        return DashboardResponse.builder()
                .projectName(project.getProjectName())
                .keywords(keywords)
                .consistencyScore(87) // 추후 project.getSimilarityScore() 로 변경 예정
                .positionX(currentX)
                .positionY(currentY)
                .referenceBrands(refBrands)
                .insights(mockInsights)
                .build();
    }
}
