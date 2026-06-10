package com.example.brandbeacon.service;

import com.example.brandbeacon.domain.Keyword;
import com.example.brandbeacon.domain.MoodboardImg;
import com.example.brandbeacon.domain.PositioningMap;
import com.example.brandbeacon.domain.Project;
import com.example.brandbeacon.dto.MoodboardResponse;
import com.example.brandbeacon.repository.MoodboardImgRepository;
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
public class MoodboardService {

    private final ProjectRepository projectRepository;
    private final MoodboardImgRepository moodboardImgRepository;
    private final PositioningMapRepository positioningMapRepository;
    private final ReferenceBrandRepository referenceBrandRepository;

    @Transactional
    public MoodboardResponse getMoodboardData(Long projectId) {

        // 1. 내 프로젝트 정보 찾아오기
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        // 2. 키워드 이름만 리스트로 뽑아내기
        List<String> keywordNames = project.getKeywords().stream()
                .map(Keyword::getKeywordName)
                .collect(Collectors.toList());

        // 3. 무드보드 이미지 주소만 리스트로 뽑아내기
        List<String> imgUrls = moodboardImgRepository.findByProject_ProjectId(projectId).stream()
                .map(MoodboardImg::getImgUrl)
                .collect(Collectors.toList());

        // 4. 포지셔닝 맵 좌표 가져오기
        // 만약 계산된 좌표가 없다면 -> 테스트용으로 정중앙 50.0, 50.0 위치로 생성해서 저장
        PositioningMap map = positioningMapRepository.findById(projectId).orElseGet(() -> {
            PositioningMap newMap = PositioningMap.builder()
                    .project(project)
                    .currentX(50.0f) // 테스트용 임시 X 좌표
                    .currentY(50.0f) // 테스트용 임시 Y 좌표
                    .build();
            return positioningMapRepository.save(newMap);
        });

        // 5. 기성 브랜드 데이터 가져오기
        List<MoodboardResponse.BrandDto> refBrands = referenceBrandRepository.findAll().stream()
                .map(brand -> MoodboardResponse.BrandDto.builder()
                        .brandName(brand.getBrandName())
                        .brandX(brand.getBrandX())
                        .brandY(brand.getBrandY())
                        .build())
                .collect(Collectors.toList());

        // 6. 모은 데이터 반환
        return MoodboardResponse.builder()
                .projectName(project.getProjectName())
                .brandIntro(project.getBrandIntro())
                .keywordNames(keywordNames)
                .moodboardImgUrls(imgUrls)
                .currentX(map.getCurrentX())
                .currentY(map.getCurrentY())
                .referenceBrands(refBrands)
                .build();
    }
}
