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

    private final RestTemplate restTemplate;

    // FastAPI가 8000 포트에서 실행 중이라고 가정
    private final String FAST_API_URL = "http://localhost:8000/api";

    @PostMapping("/generate-moodboard")
    public ResponseEntity<?> generateMoodboard(@RequestBody Map<String, Object> request) {
        String targetUrl = FAST_API_URL + "/generate-moodboard";
        return restTemplate.postForEntity(targetUrl, request, Object.class);
    }

    @PostMapping("/analyze-dashboard")
    public ResponseEntity<?> analyzeDashboard(@RequestBody Map<String, Object> request) {
        String targetUrl = FAST_API_URL + "/analyze-dashboard";
        return restTemplate.postForEntity(targetUrl, request, Object.class);
    }
}