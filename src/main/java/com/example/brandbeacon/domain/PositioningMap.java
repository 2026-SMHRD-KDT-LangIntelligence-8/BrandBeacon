package com.example.brandbeacon.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "POSITIONING_MAP")
public class PositioningMap {

    @Id
    @Column(name = "PROJECT_ID")
    private Long projectId; // 프로젝트 번호를 그대로 PK로 사용 (1:1 관계)

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Project의 PK를 그대로 식별자로 사용하겠다는 설정
    @JoinColumn(name = "PROJECT_ID")
    private Project project; // 대상 프로젝트 연결

    @Column(name = "CURRENT_X", nullable = false)
    private Float currentX; // 생성된 브랜드의 X 좌표 값

    @Column(name = "CURRENT_Y", nullable = false)
    private Float currentY; // 생성된 브랜드의 Y 좌표 값
}