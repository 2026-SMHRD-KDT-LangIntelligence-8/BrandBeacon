package com.example.brandbeacon.controller;

import com.example.brandbeacon.dto.GenerateMoodboardRequestDto;
import com.example.brandbeacon.dto.PythonMoodboardResponseDto;
import com.example.brandbeacon.service.AiModelService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/moodboard") // 기존 조회 API와 경로를 분리합니다.
@RequiredArgsConstructor
public class AiMoodboardController {

    private final AiModelService aiModelService;

    // AI 이미지 생성 API
    // -> POST 요청 (데이터를 서버로 보내서 무언가 생성할 때는 POST를 씁니다)
    @PostMapping("/generate")
    public ResponseEntity<?> generateAiMoodboard(
            @RequestBody GenerateMoodboardRequestDto requestDto,
            HttpServletRequest httpRequest) {

        try {
            // 1. 기존 프로젝트 규칙에 맞춘 세션(로그인) 확인 로직
            HttpSession session = httpRequest.getSession(false);
            if (session == null || session.getAttribute("loginUserId") == null) {
                return ResponseEntity.status(401).body("로그인이 필요한 서비스입니다.");
            }

            // 2. 입력받은 데이터를 Service로 넘겨 파이썬 API 호출 및 결과 수신
            PythonMoodboardResponseDto response = aiModelService.getMoodboardFromPython(requestDto);

            // 3. 파이썬에서 무사히 결과가 왔다면 프론트엔드로 200 OK와 함께 전송!
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 파이썬 서버가 꺼져있거나, 통신 타임아웃이 발생했을 때의 에러 처리
            return ResponseEntity.internalServerError().body("AI 이미지 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}