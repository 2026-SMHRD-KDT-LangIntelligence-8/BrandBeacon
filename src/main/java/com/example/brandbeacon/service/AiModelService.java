package com.example.brandbeacon.service;

import com.example.brandbeacon.dto.GenerateMoodboardRequestDto;
import com.example.brandbeacon.dto.PythonMoodboardResponseDto;
import org.springframework.beans.factory.annotation.Qualifier; // ⭐️ 추가
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
// ⭐️ 주의: @RequiredArgsConstructor는 지워주세요. (우리가 직접 생성자를 만들 것입니다)
public class AiModelService {

    private final RestTemplate restTemplate;
    private final String PYTHON_SERVER_URL = "http://localhost:8000/api/ai/moodboard/generate";

    // ⭐️ 생성자에 @Qualifier를 붙여서 우리가 만든 AI 전용 빈을 콕 집어 가져옵니다.
    public AiModelService(@Qualifier("aiRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PythonMoodboardResponseDto getMoodboardFromPython(GenerateMoodboardRequestDto requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GenerateMoodboardRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<PythonMoodboardResponseDto> response = restTemplate.postForEntity(
                PYTHON_SERVER_URL,
                requestEntity,
                PythonMoodboardResponseDto.class
        );

        return response.getBody();
    }
}