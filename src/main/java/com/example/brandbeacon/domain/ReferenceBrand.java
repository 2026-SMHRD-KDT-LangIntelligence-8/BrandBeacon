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
@Table(name = "REFERENCE_BRAND")
public class ReferenceBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BRAND_ID")
    private Long brandId; // 기성 브랜드 고유 번호 (PK)

    @Column(name = "CATEGORY", nullable = false, length = 100)
    private String category; // 브랜드 카테고리

    @Column(name = "BRAND_NAME", nullable = false, length = 100, unique = true)
    private String brandName; // 기성 브랜드 이름

    @Column(name = "BRAND_X", nullable = false)
    private Float brandX; // 기성 브랜드의 X 좌표 고정값

    @Column(name = "BRAND_Y", nullable = false)
    private Float brandY; // 기성 브랜드의 Y 좌표 고정값
}