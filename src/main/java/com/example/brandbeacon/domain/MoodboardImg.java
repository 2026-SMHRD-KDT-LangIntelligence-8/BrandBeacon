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
@Table(name = "MOODBOARD_IMG")
public class MoodboardImg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IMG_ID")
    private Long imgId; // 이미지 고유 번호 (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Project project; // 어떤 프로젝트에 속한 이미지인지 연결

    @Column(name = "IMG_URL", nullable = false, length = 2048)
    private String imgUrl; // 이미지 파일이 저장된 S3 또는 웹 URL 주소
}