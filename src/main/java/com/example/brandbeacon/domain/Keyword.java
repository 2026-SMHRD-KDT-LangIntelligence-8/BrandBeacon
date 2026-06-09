package com.example.brandbeacon.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "KEYWORD")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "KEYWORD_ID")
    private Long keywordId;

    @Column(name = "KEYWORD_NAME", nullable = false, unique = true, length = 50)
    private String keywordName;

    @Column(name = "KEYWORD_IMG_URL", length = 2048)
    private String keywordImgUrl;
}