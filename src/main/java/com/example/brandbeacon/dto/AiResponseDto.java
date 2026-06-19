package com.example.brandbeacon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Map;

@Getter
@NoArgsConstructor
public class AiResponseDto {

    // Python: "brandProfile": { "category": "...", "description": "...", ... }
    private Map<String, Object> brandProfile;

    // Python: "consistency": { "score": 85, "grade": "A", ... }
    private ConsistencyDto consistency;

    // Python: "position": { "x": 0.12, "y": -0.34, "quadrant": "Q1", ... }
    private PositionDto position;

    // Python: "insight": { "종합평가": "...", "강점": [...], "기회": [...], ... }
    private Map<String, Object> insight;

    @Getter
    @NoArgsConstructor
    public static class ConsistencyDto {
        private Integer score;
        private String grade;
    }

    @Getter
    @NoArgsConstructor
    public static class PositionDto {
        private Float x;
        private Float y;
        private String quadrant;
    }
}
