package com.example.brandbeacon.service;

import com.example.brandbeacon.domain.*;
import com.example.brandbeacon.dto.ProjectCreateRequest;
import com.example.brandbeacon.dto.ProjectDetailResponse;
import com.example.brandbeacon.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final KeywordRepository keywordRepository;
    private final MoodboardImgRepository moodboardImgRepository;
    private final FolderRepository folderRepository;

    private final PositioningMapRepository positioningMapRepository;


    // 프로젝트 생성 로직
    @Transactional // 로직 수행 중 에러가 발생하면 전체 데이터를 롤백시키는 어노테이션
    public Long createProject(Long userId, ProjectCreateRequest request) {

        // 1. 세션에서 가져온 유저 ID
        // -> 실제 DB에 존재하는 회원인지 확인 후 불러옴
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 정보가 유효하지 않습니다. 다시 로그인해주세요."));

        // 동일 유저 내 프로젝트명 중복 확인
        if (projectRepository.existsByMember_IdAndProjectName(userId, request.getProjectName())) {
            throw new IllegalArgumentException("이미 사용 중인 프로젝트 이름입니다. 다른 이름을 입력해 주세요.");
        }

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
                .moodboardData(request.getMoodboardData()) // 카테고리별 무드보드 구조
                .imageAlignmentsData(request.getImageAlignmentsData()) // 이미지 유사도 데이터
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


        // 프론트에서 전달한 AI 분석 결과를 바로 저장 (재호출 없음)
        BigDecimal score = request.getSimilarityScore() != null
                ? new BigDecimal(request.getSimilarityScore())
                : BigDecimal.ZERO;
        String insightText = request.getAnalysisInsight() != null
                ? request.getAnalysisInsight()
                : "분석 내용이 없습니다.";

        savedProject.updateAiAnalysis(score, insightText);

        if (request.getBrandProfile() != null) {
            savedProject.updateBrandProfile(request.getBrandProfile());
        }

        Float posX = request.getPositionX() != null ? request.getPositionX() : 50.0f;
        Float posY = request.getPositionY() != null ? request.getPositionY() : 50.0f;
        PositioningMap posMap = PositioningMap.builder()
                .project(savedProject)
                .currentX(posX)
                .currentY(posY)
                .build();
        positioningMapRepository.save(posMap);


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
                .moodboardData(project.getMoodboardData())
                .imageAlignmentsData(project.getImageAlignmentsData())
                .createdAt(project.getCreatedAt())
                .build();
    }

    // 프로젝트 삭제 로직
    @Transactional
    public void deleteProject(Long projectId, Long userId) {

        // 삭제하려는 프로젝트를 DB에서 찾아오기
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        // 내 프로젝트가 맞는지 확인
        if (!project.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 생성한 프로젝트만 삭제할 수 있습니다.");
        }

        // FK 제약 순서에 맞게 연관 데이터 먼저 삭제
        moodboardImgRepository.deleteByProject_ProjectId(projectId);
        positioningMapRepository.deleteById(projectId);

        // 프로젝트 삭제 (PROJECT_KEYWORD 중간 테이블은 JPA가 자동 처리)
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
                    .folderId(project.getFolder() != null ? project.getFolder().getFolderId() : null)
                    .projectName(project.getProjectName())
                    .brandIntro(project.getBrandIntro())
                    .referenceType(project.getReferenceType())
                    .brandProfile(project.getBrandProfile())
                    .keywords(keywordNames)
                    .imgUrls(imgUrls)
                    .moodboardData(project.getMoodboardData())
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

    // 프로젝트 폴더 이동
    @Transactional
    public void moveProjectToFolder(Long projectId, Long userId, Long folderId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        if (!project.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 생성한 프로젝트만 이동할 수 있습니다.");
        }

        Folder folder = null;
        if (folderId != null) {
            folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        }

        project.updateFolder(folder);
    }
}