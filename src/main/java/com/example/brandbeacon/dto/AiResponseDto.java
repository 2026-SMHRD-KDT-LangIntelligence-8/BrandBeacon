package com.example.brandbeacon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class AiResponseDto {
    private BigDecimal consistencyScore; // 일관성 점수
    private Float positionX;             // X 좌표
    private Float positionY;             // Y 좌표
    private List<String> insights;       // AI 분석 텍스트 리스트
}
