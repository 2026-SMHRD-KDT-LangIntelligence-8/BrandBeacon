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
}