package com.example.brandbeacon.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "PROJECT")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROJECT_ID")
    private Long projectId; // 프로젝트 고유 번호 (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Member member; // 프로젝트를 생성한 회원 (USER 테이블과 연관관계)

    @Column(name = "PROJECT_NAME", nullable = false, length = 255)
    private String projectName; // 프로젝트 이름

    @Column(name = "BRAND_INTRO", nullable = false, length = 500)
    private String brandIntro; // 브랜드 소개글

    @Column(name = "REFERENCE_TYPE", nullable = false, length = 1000)
    private String referenceType; // 레퍼런스 희망 항목

    @Column(name = "SIMILARITY_SCORE", precision = 5, scale = 2)
    private BigDecimal similarityScore; // AI가 계산한 유사도 점수

    @Column(name = "ANALYSIS_INSIGHT", length = 1000)
    private String analysisInsight; // AI 분석 인사이트 내용

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt; // 프로젝트 생성 일자


    // 프로젝트와 키워드 관계 연결하는 중간 매핑
    @ManyToMany // 다대다 관계 연결
    @JoinTable(
            name = "PROJECT_KEYWORD",
            joinColumns = @JoinColumn(name = "PROJECT_ID"),
            inverseJoinColumns = @JoinColumn(name = "KEYWORD_ID")
    )
    private List<Keyword> keywords; // 선택한 키워드 리스트


    // 파이썬 AI 결과를 받아와 인사이트를 채워넣는 메서드
    public void updateAiAnalysis(java.math.BigDecimal similarityScore, String analysisInsight) {
        this.similarityScore = similarityScore;
        this.analysisInsight = analysisInsight;
    }

}