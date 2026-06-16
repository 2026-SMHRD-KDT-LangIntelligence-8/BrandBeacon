package com.example.brandbeacon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiProxyController {

    // 💡 수정: Config에서 지정한 Bean 이름("aiRestTemplate")과 변수명을 똑같이 맞춰줍니다.
    private final RestTemplate aiRestTemplate;

    private final String FAST_API_URL = "http://localhost:8000/api";

    @PostMapping("/generate-moodboard")
    public ResponseEntity<?> generateMoodboard(@RequestBody Map<String, Object> request) {
        String targetUrl = FAST_API_URL + "/generate-moodboard";
        // 💡 수정: aiRestTemplate 사용
        return aiRestTemplate.postForEntity(targetUrl, request, Object.class);
    }

    @PostMapping("/analyze-dashboard")
    public ResponseEntity<?> analyzeDashboard(@RequestBody Map<String, Object> request) {
        String targetUrl = FAST_API_URL + "/analyze-dashboard";
        // 💡 수정: aiRestTemplate 사용
        return aiRestTemplate.postForEntity(targetUrl, request, Object.class);
    }

    // 💡 브랜드 분석 전체 파이프라인 (벡터 생성 → 포지셔닝 분석) - DB 저장 없이 결과만 반환
    @PostMapping("/brand/analyze-full")
    public ResponseEntity<?> analyzeBrandFull(@RequestBody Map<String, Object> request) {
        try {
            // Step 1: 브랜드 벡터 생성
            request.put("project_id", "preview");
            ResponseEntity<Map> vectorResponse = aiRestTemplate.postForEntity(
                    FAST_API_URL + "/brand/vector", request, Map.class
            );
            Map<?, ?> vectorData = vectorResponse.getBody();

            // Step 2: 브랜드 분석 실행
            Map<String, Object> analyzeRequest = new java.util.HashMap<>();
            analyzeRequest.put("project_id", "preview");
            analyzeRequest.put("brand_vector", vectorData.get("brand_vector"));
            analyzeRequest.put("brand_profile", vectorData.get("brand_profile"));
            analyzeRequest.put("user_text", request.get("user_text"));
            analyzeRequest.put("selected_tags", request.get("selected_tags"));
            analyzeRequest.put("image_alignments", vectorData.get("image_alignments"));

            return aiRestTemplate.postForEntity(
                    FAST_API_URL + "/brand/analyze", analyzeRequest, Object.class
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body("분석 중 오류: " + e.getMessage());
        }
    }
}