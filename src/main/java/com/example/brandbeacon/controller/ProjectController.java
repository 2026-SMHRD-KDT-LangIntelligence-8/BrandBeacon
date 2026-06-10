package com.example.brandbeacon.controller;

import com.example.brandbeacon.dto.ProjectCreateRequest;
import com.example.brandbeacon.dto.ProjectDetailResponse;
import com.example.brandbeacon.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // JSON 형태의 객체 데이터를 반환하는 컨트롤러
@RequestMapping("/api/projects")
@RequiredArgsConstructor // 의존성 주입을 위한 어노테이션
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<String> createProject(@RequestBody ProjectCreateRequest request, HttpServletRequest httpRequest) {
        try {
            // 1. 요청 객체로부터 현재 브라우저의 세션 가져오기
            // 없으면 새로 만들지 않고 null 반환
            HttpSession session = httpRequest.getSession(false);

            // 2. 세션이 존재하지 않거나 세션 내에 로그인된 유저 ID 정보가 없다면 -> 인증 에러(401) 반환
            if (session == null || session.getAttribute("loginUserId") == null) {
                return ResponseEntity.status(401).body("로그인이 필요한 서비스입니다.");
            }

            // 3. 세션 방명록에 저장해 두었던 유저 고유 번호(ID)를 꺼내오기
            Long userId = (Long) session.getAttribute("loginUserId");

            // 4. 추출한 유저 ID와 프론트엔드에서 보낸 프로젝트 데이터를 서비스 레이어로 전달하여 생성 로직 수행
            Long projectId = projectService.createProject(userId, request);

            // 5. 성공적으로 처리 완료 시
            // -> 생성된 프로젝트 ID와 함께 성공 메시지를 반환
            return ResponseEntity.ok("프로젝트가 성공적으로 생성되었습니다! 프로젝트 ID: " + projectId);

        } catch (IllegalArgumentException e) {
            // 유저 정보 유효성 검증 실패 또는 에러 발생 시
            // -> 잘못된 요청(400)과 에러 메시지를 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 특정 프로젝트 상세 조회 API (GET 요청)
    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProjectDetail(
            @PathVariable("projectId") Long projectId,
            HttpServletRequest httpRequest) {
        try {
            // 1. 세션(방명록) 확인 및 로그인 상태 검증
            HttpSession session = httpRequest.getSession(false);
            if (session == null || session.getAttribute("loginUserId") == null) {
                return ResponseEntity.status(401).body("로그인이 필요한 서비스입니다.");
            }

            // 2. 세션에서 현재 로그인한 유저 ID 꺼내기
            Long userId = (Long) session.getAttribute("loginUserId");

            // 3. 추출한 유저 ID와 프로젝트 ID를 서비스 레이어로 넘겨서 상세 데이터 받아오기
            ProjectDetailResponse response = projectService.getProjectDetail(projectId, userId);

            // 4. 성공 시 프론트엔드로 상세 데이터 반환
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // 본인이 생성하지 않은 프로젝트에 접근하거나 존재하지 않는 프로젝트일 경우 에러 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 내 프로젝트 삭제
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(
            @PathVariable("projectId") Long projectId, // URL 경로의 프로젝트 번호
            HttpServletRequest httpRequest) {
        try {
            // 세션 검사
            // -> 로그인 안 한 사람은 남의 걸 지울 수 없도록
            HttpSession session = httpRequest.getSession(false);
            if (session == null || session.getAttribute("loginUserId") == null) {
                return ResponseEntity.status(401).body("로그인이 필요한 서비스입니다.");
            }
            Long userId = (Long) session.getAttribute("loginUserId");

            // 서비스로 삭제 지시
            projectService.deleteProject(projectId, userId);

            return ResponseEntity.ok("프로젝트가 성공적으로 삭제되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}