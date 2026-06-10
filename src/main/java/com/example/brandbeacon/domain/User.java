package com.example.brandbeacon.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "NICKNAME", nullable = false, length = 50)
    private String nickname;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "OAUTH_ID", length = 255)
    private String oauthId;

    @Column(name = "JOINED_AT")
    private LocalDateTime joinedAt = LocalDateTime.now();
}