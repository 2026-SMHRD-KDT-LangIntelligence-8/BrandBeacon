package com.example.brandbeacon.controller;

import com.example.brandbeacon.dto.MoodboardResponse;
import com.example.brandbeacon.service.MoodboardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/moodboard")
@RequiredArgsConstructor
public class MoodboardController {

    private final MoodboardService moodboardService;

    // 무드보드 종합 데이터 조회 API
    // -> GET 요청
    @GetMapping
    public ResponseEntity<?> getMoodboard(
            @PathVariable("projectId") Long projectId, // URL에 있는 프로젝트 번호를 가져옵니다.
            HttpServletRequest httpRequest) {

        try {
            // 1. 세션이 있는지, 로그인한 사람인지 확인하기
            HttpSession session = httpRequest.getSession(false);
            if (session == null || session.getAttribute("loginUserId") == null) {
                return ResponseEntity.status(401).body("로그인이 필요한 서비스입니다.");
            }

            // 2. 프로젝트 번호를 전송 후, 데이터 받아오기
            MoodboardResponse response = moodboardService.getMoodboardData(projectId);

            // 3. 성공적으로 가져왔다면 프론트엔드로 전송!
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // 없는 프로젝트 번호 요청했을 때 에러 처리
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
