package com.example.brandbeacon.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROJECT")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROJECT_ID")
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "PROJECT_NAME", nullable = false, length = 255)
    private String projectName;

    @Column(name = "BRAND_INTRO", nullable = false, length = 500)
    private String brandIntro;

    @Column(name = "REFERENCE_TYPE", nullable = false, length = 1000)
    private String referenceType;

    @Column(name = "SIMILARITY_SCORE")
    private Double similarityScore;

    @Column(name = "ANALYSIS_INSIGHT", columnDefinition = "TEXT")
    private String analysisInsight;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt = LocalDateTime.now();
}