package com.example.brandbeacon.service;

import com.example.brandbeacon.domain.Keyword;
import com.example.brandbeacon.domain.Member;
import com.example.brandbeacon.domain.MoodboardImg;
import com.example.brandbeacon.domain.Project;
import com.example.brandbeacon.domain.PositioningMap; // ✨ NEW: 맵 엔티티 임포트
import com.example.brandbeacon.dto.ProjectCreateRequest;
import com.example.brandbeacon.dto.ProjectDetailResponse;
import com.example.brandbeacon.dto.AiRequestDto; // ✨ NEW: 파이썬 요청 가방 임포트
import com.example.brandbeacon.dto.AiResponseDto; // ✨ NEW: 파이썬 응답 가방 임포트
import com.example.brandbeacon.repository.KeywordRepository;
import com.example.brandbeacon.repository.MemberRepository;
import com.example.brandbeacon.repository.MoodboardImgRepository;
import com.example.brandbeacon.repository.ProjectRepository;
import com.example.brandbeacon.repository.PositioningMapRepository; // ✨ NEW: 맵 창고 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate; // ✨ NEW: 심부름꾼 임포트

import java.util.ArrayList; // ✨ NEW: 빈 리스트 처리를 위한 임포트
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final KeywordRepository keywordRepository;
    private final MoodboardImgRepository moodboardImgRepository;

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


        // 2. DB에서 실제 키워드 엔티티 목록을 한 번에 조회 (✨ 빈 값 전달 시 500 에러 방어 로직 적용)
        List<Keyword> keywords = (request.getKeywordIds() != null && !request.getKeywordIds().isEmpty())
                ? keywordRepository.findAllById(request.getKeywordIds())
                : new ArrayList<>();


        // 3. 빌더 패턴을 사용 -> 새로운 프로젝트(Project) 객체를 조립
        Project project = Project.builder()
                .member(member)
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


        // 파이썬 AI 서버 연동
        try {
            // 1. 파이썬에 보낼 키워드 이름 쏙쏙 뽑아내기
            List<String> keywordNames = keywords.stream()
                    .map(Keyword::getKeywordName)
                    .collect(Collectors.toList());

            // 2. 파이썬에 보낼 데이터 정의
            AiRequestDto aiRequest = AiRequestDto.builder()
                    .keywords(keywordNames)
                    .imageUrls(request.getImgUrls())
                    .build();

            // 파이썬 서버 -> POST 요청 전송
            String pythonApiUrl = "http://localhost:8000/api/analyze";
            AiResponseDto aiResponse = restTemplate.postForObject(pythonApiUrl, aiRequest, AiResponseDto.class);

            // 4. 결과물 DB에 업데이트
            if (aiResponse != null) {
                // 프로젝트 엔티티에 일관성 점수와 인사이트 1번 문구 저장
                String insightText = (aiResponse.getInsights() != null && !aiResponse.getInsights().isEmpty())
                        ? aiResponse.getInsights().get(0) : "분석 내용이 없습니다.";

                savedProject.updateAiAnalysis(aiResponse.getConsistencyScore(), insightText);

                // 포지셔닝 맵 좌표 생성 후 DB에 저장
                PositioningMap map = PositioningMap.builder()
                        .project(savedProject)
                        .currentX(aiResponse.getPositionX())
                        .currentY(aiResponse.getPositionY())
                        .build();
                positioningMapRepository.save(map);
            }

        } catch (Exception e) {
            // 만약 파이썬 서버가 꺼져있거나 에러가 나도
            // 프로젝트 생성이 취소되지 않도록 에러만 로그로 남기기
            System.out.println("AI 파이썬 서버 연동 실패: " + e.getMessage());

            // AI 서버 연동 실패 시 프론트엔드 테스트를 위한 임시 좌표
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

        // 5. DTO 객체로 반환
        return ProjectDetailResponse.builder()
                .projectId(project.getProjectId())
                .projectName(project.getProjectName())
                .brandIntro(project.getBrandIntro())
                .referenceType(project.getReferenceType()) // 👈 request 대신 이미 조회한 project 객체 사용
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
}