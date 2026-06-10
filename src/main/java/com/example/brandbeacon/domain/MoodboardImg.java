package com.example.brandbeacon.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MOODBOARD_IMG")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodboardImg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IMG_ID")
    private Long imgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Project project;

    @Column(name = "IMG_URL", nullable = false, length = 2048)
    private String imgUrl;
}