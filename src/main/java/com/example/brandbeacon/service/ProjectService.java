package com.example.brandbeacon.service;

import com.example.brandbeacon.domain.*;
import com.example.brandbeacon.dto.ProjectCreateRequest;
import com.example.brandbeacon.dto.ProjectDetailResponse;
import com.example.brandbeacon.dto.AiResponseDto;
import com.example.brandbeacon.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final KeywordRepository keywordRepository;
    private final MoodboardImgRepository moodboardImgRepository;
    private final FolderRepository folderRepository;

    // AI 연동을 위해 포지셔닝 맵 추가
    private final RestTemplate restTemplate;
    private final PositioningMapRepository positioningMapRepository;


    // 프로젝트 생성 로직
    @Transactional // 로직 수행 중 에러가 발생하면 전체 데이터를 롤백시키는 어노테이션
    public Long createProject(Long userId, ProjectCreateRequest request) {

        // 1. 세션에서 가져온 유저 ID
        // -> 실제 DB에 존재하는 회원인지 확인 후 불러옴
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 정보가 유효하지 않습니다. 다시 로그인해주세요."));

        // 폴더 정보 조회
        Folder folder = null;
        if (request.getFolderId() != null) {
            folder = folderRepository.findById(request.getFolderId())
                    .orElse(null); // 폴더가 없으면 null 유지 (전체 프로젝트로 처리)
        }

        // 2. DB에서 실제 키워드 엔티티 목록을 한 번에 조회
        List<Keyword> keywords = new ArrayList<>();
        if (request.getKeywordIds() != null && !request.getKeywordIds().isEmpty()) {
            List<String> keywordNames = request.getKeywordIds();
            keywords = keywordRepository.findAll().stream()
                    .filter(k -> keywordNames.contains(k.getKeywordName()))
                    .collect(Collectors.toList());
        }


        // 3. 빌더 패턴을 사용 -> 새로운 프로젝트(Project) 객체를 조립
        Project project = Project.builder()
                .member(member)
                .folder(folder) // 폴더 연관관계 매핑
                .projectName(request.getProjectName())
                .brandIntro(request.getBrandIntro())
                .referenceType(request.getReferenceType())
                .keywords(keywords) // 다대다(ManyToMany) 관계 매핑
                .build();


        // 4. 프로젝트 객체를 DB에 저장하고, 자동 생성된 프로젝트 고유 ID가 포함된 객체 받기
        Project savedProject = projectRepository.save(project);


        // 5. 무드보드 이미지 URL 목록이 포함되어 있다면 -> 반복문을 돌며 각각 저장
        if (request.getImgUrls() != null && !request.getImgUrls().isEmpty()) {
            for (String url : request.getImgUrls()) {
                MoodboardImg moodboardImg = MoodboardImg.builder()
                        .project(savedProject) // 방금 저장한 프로젝트와 연관관계 매핑
                        .imgUrl(url)
                        .build();
                moodboardImgRepository.save(moodboardImg);
            }
        }


        // 파이썬 AI 서버 연동 (brand-dashboard, 2단계 파이프라인)
        try {
            ObjectMapper mapper = new ObjectMapper();

            List<String> keywordNames = keywords.stream()
                    .map(Keyword::getKeywordName)
                    .collect(Collectors.toList());

            // 1단계: 브랜드 벡터 생성 (POST /api/brand/vector)
            Map<String, Object> vectorRequest = new HashMap<>();
            vectorRequest.put("project_id", savedProject.getProjectId().toString());
            vectorRequest.put("user_text", request.getBrandIntro());
            vectorRequest.put("selected_tags", keywordNames);
            vectorRequest.put("image_urls", request.getImgUrls());

            @SuppressWarnings("unchecked")
            Map<String, Object> vectorResponse = (Map<String, Object>) restTemplate.postForObject(
                    "http://localhost:8001/api/brand/vector", vectorRequest, Map.class);

            // 2단계: 브랜드 분석 (POST /api/brand/analyze)
            Map<String, Object> analyzeRequest = new HashMap<>();
            analyzeRequest.put("project_id", savedProject.getProjectId().toString());
            analyzeRequest.put("brand_vector", vectorResponse.get("brand_vector"));
            analyzeRequest.put("brand_profile", vectorResponse.get("brand_profile"));
            analyzeRequest.put("user_text", request.getBrandIntro());
            analyzeRequest.put("selected_tags", keywordNames);
            analyzeRequest.put("image_alignments", vectorResponse.get("image_alignments"));

            AiResponseDto aiResponse = restTemplate.postForObject(
                    "http://localhost:8001/api/brand/analyze", analyzeRequest, AiResponseDto.class);

            // 결과물 DB에 저장
            if (aiResponse != null) {
                // insight: JSON 객체 → 문자열로 변환해서 TEXT 컬럼에 저장
                String insightText = aiResponse.getInsight() != null
                        ? mapper.writeValueAsString(aiResponse.getInsight())
                        : "분석 내용이 없습니다.";

                BigDecimal score = (aiResponse.getConsistency() != null && aiResponse.getConsistency().getScore() != null)
                        ? new BigDecimal(aiResponse.getConsistency().getScore())
                        : BigDecimal.ZERO;

                savedProject.updateAiAnalysis(score, insightText);

                // brandProfile: dict → JSON 문자열로 변환
                if (aiResponse.getBrandProfile() != null) {
                    savedProject.updateBrandProfile(mapper.writeValueAsString(aiResponse.getBrandProfile()));
                }

                // 포지셔닝 맵 좌표 저장
                Float posX = (aiResponse.getPosition() != null) ? aiResponse.getPosition().getX() : 50.0f;
                Float posY = (aiResponse.getPosition() != null) ? aiResponse.getPosition().getY() : 50.0f;
                PositioningMap map = PositioningMap.builder()
                        .project(savedProject)
                        .currentX(posX)
                        .currentY(posY)
                        .build();
                positioningMapRepository.save(map);
            }

        } catch (Exception e) {
            System.out.println("AI 파이썬 서버 연동 실패: " + e.getMessage());

            PositioningMap fallbackMap = PositioningMap.builder()
                    .project(savedProject)
                    .currentX(50.0f)
                    .currentY(50.0f)
                    .build();
            positioningMapRepository.save(fallbackMap);
        }


        // 생성된 프로젝트의 ID 번호를 반환
        return savedProject.getProjectId();
    }

    // 내 프로젝트 상세 조회 로직
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectDetail(Long projectId, Long userId) {

        // 1. 프로젝트 번호로 DB에서 프로젝트 찾기
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        // 2. 프로젝트를 만든 사람과 현재 로그인한 사람(userId)이 같은지 확인!
        if (!project.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 생성한 프로젝트만 열람할 수 있습니다.");
        }

        // 3. 키워드 이름 리스트로 추출
        List<String> keywordNames = project.getKeywords().stream()
                .map(Keyword::getKeywordName)
                .collect(Collectors.toList());

        // 4. 이미지 URL만 리스트로 추출
        List<String> imgUrls = moodboardImgRepository.findByProject_ProjectId(projectId).stream()
                .map(MoodboardImg::getImgUrl)
                .collect(Collectors.toList());

        // 5. 포지셔닝 맵 좌표 조회
        PositioningMap posMap = positioningMapRepository.findById(projectId).orElse(null);

        // 6. DTO 객체로 반환
        return ProjectDetailResponse.builder()
                .projectId(project.getProjectId())
                .projectName(project.getProjectName())
                .brandIntro(project.getBrandIntro())
                .referenceType(project.getReferenceType())
                .brandProfile(project.getBrandProfile())
                .analysisInsight(project.getAnalysisInsight())
                .similarityScore(project.getSimilarityScore())
                .positionX(posMap != null ? posMap.getCurrentX() : null)
                .positionY(posMap != null ? posMap.getCurrentY() : null)
                .keywords(keywordNames)
                .imgUrls(imgUrls)
                .createdAt(project.getCreatedAt())
                .build();
    }

    // 프로젝트 삭제 로직
    @Transactional
    public void deleteProject(Long projectId, Long userId) {

        // 삭제하려는 프로젝트를 DB에서 찾아오기
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        // 내 프로젝트가 맞는지 확인!
        // 만약 남의 프로젝트면 에러 뱉음.
        if (!project.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 생성한 프로젝트만 삭제할 수 있습니다.");
        }

        // 문제없으면 DB에서 시원하게 영구 삭제
        // 연결된 이미지 데이터들도 같이 삭제
        projectRepository.delete(project);
    }

    // 마이페이지 프로젝트 저장소 목록 조회를 위한 로직 추가
    @Transactional(readOnly = true)
    public List<ProjectDetailResponse> getMyProjects(Long userId) {
        List<Project> projects = projectRepository.findByMember_Id(userId);

        return projects.stream().map(project -> {
            List<String> keywordNames = project.getKeywords().stream()
                    .map(Keyword::getKeywordName)
                    .collect(Collectors.toList());

            List<String> imgUrls = moodboardImgRepository.findByProject_ProjectId(project.getProjectId()).stream()
                    .map(MoodboardImg::getImgUrl)
                    .collect(Collectors.toList());

            return ProjectDetailResponse.builder()
                    .projectId(project.getProjectId())
                    .projectName(project.getProjectName())
                    .brandIntro(project.getBrandIntro())
                    .referenceType(project.getReferenceType())
                    .brandProfile(project.getBrandProfile())
                    .keywords(keywordNames)
                    .imgUrls(imgUrls)
                    .createdAt(project.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    // 특정 폴더에 속한 프로젝트 목록 조회
    @Transactional(readOnly = true)
    public List<ProjectDetailResponse> getProjectsByFolder(Long folderId, Long userId) {
        List<Project> projects = projectRepository.findByFolder_FolderId(folderId);

        return projects.stream()
                .filter(p -> p.getMember().getId().equals(userId))
                .map(project -> {
                    List<String> keywordNames = project.getKeywords().stream()
                            .map(Keyword::getKeywordName)
                            .collect(Collectors.toList());

                    List<String> imgUrls = moodboardImgRepository.findByProject_ProjectId(project.getProjectId()).stream()
                            .map(MoodboardImg::getImgUrl)
                            .collect(Collectors.toList());

                    return ProjectDetailResponse.builder()
                            .projectId(project.getProjectId())
                            .projectName(project.getProjectName())
                            .brandIntro(project.getBrandIntro())
                            .referenceType(project.getReferenceType())
                            .brandProfile(project.getBrandProfile())
                            .keywords(keywordNames)
                            .imgUrls(imgUrls)
                            .createdAt(project.getCreatedAt())
                            .build();
                }).collect(Collectors.toList());
    }
}