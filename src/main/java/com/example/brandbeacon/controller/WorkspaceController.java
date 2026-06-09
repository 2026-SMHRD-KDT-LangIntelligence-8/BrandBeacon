package com.example.brandbeacon.controller;

import com.example.brandbeacon.domain.Project;
import com.example.brandbeacon.domain.User;
import com.example.brandbeacon.repository.ProjectRepository;
import com.example.brandbeacon.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/workspace")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*") 차후에 주석 해제할 것
public class WorkspaceController {

    // 새로운 DB 구조에 맞는 레포지토리만 주입받습니다.
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // 테스트용 유저를 가져오는 헬퍼 메서드 (User 엔티티 사용)
    private User getTestUser() {
        return userRepository.findByEmail("test@test.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("test@test.com")
                        .nickname("테스터")
                        .password("1234")
                        .build()));
    }

    // 1. 프로젝트 신규 저장
    @PostMapping("/projects")
    public ResponseEntity<?> saveProject(@RequestBody ProjectSaveDto dto) {
        User user = getTestUser();

        // 빌더 패턴으로 새로운 Project 엔티티 생성
        Project project = Project.builder()
                .user(user)
                .projectName(dto.getTitle()) // DTO의 title을 매핑
                .brandIntro("기본 소개")      // 명세서 필드 대응 (필요시 DTO에서 받아오도록 수정 가능)
                .referenceType(dto.getMoodboardData()) // 명세서 필드 대응
                .analysisInsight(dto.getAnalysisData())
                .createdAt(LocalDateTime.now())
                .build();

        projectRepository.save(project);
        return ResponseEntity.ok("성공적으로 저장되었습니다.");
    }

    // 2. 내 프로젝트 전체 목록 조회
    @GetMapping("/projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectRepository.findByUser(getTestUser()));
    }

    // 데이터 전송 객체 (DTO)
    @Data
    public static class ProjectSaveDto {
        private String title;
        private String moodboardData; // 기존 데이터 매핑
        private String analysisData;  // 기존 데이터 매핑
        // 필요한 경우 여기에 필드를 추가하여 DTO를 확장하세요.
    }
}