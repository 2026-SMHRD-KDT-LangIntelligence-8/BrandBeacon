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
@Table(name = "KEYWORD")
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "KEYWORD_ID")
    private Long keywordId; // 키워드 고유 번호 (PK)

    @Column(name = "KEYWORD_NAME", nullable = false, length = 50, unique = true)
    private String keywordName; // 키워드 이름

    @Column(name = "KEYWORD_IMG_URL", length = 2048)
    private String keywordImgUrl; // 키워드 예시 이미지 URL
}