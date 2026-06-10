package com.example.brandbeacon.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class AiRequestDto {
    private List<String> keywords; // 선택한 키워드들 (예: ["ACTIVE", "dynamic"])
    private List<String> imageUrls; // 업로드한 무드보드 이미지 주소들
}
