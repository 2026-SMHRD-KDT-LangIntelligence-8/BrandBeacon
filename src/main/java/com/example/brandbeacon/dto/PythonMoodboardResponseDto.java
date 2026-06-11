package com.example.brandbeacon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class PythonMoodboardResponseDto {

    private String status;

    // 카테고리별(essence, product 등) 이미지 URL 리스트와 palette를 담습니다.
    private Map<String, List<String>> images;

    // LLM이 생성한 쿼리 정보들을 담습니다.
    private Map<String, Object> queries;
}
