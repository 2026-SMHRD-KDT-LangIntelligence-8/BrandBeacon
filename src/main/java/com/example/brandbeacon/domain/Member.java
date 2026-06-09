package com.example.brandbeacon.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor // 기본 생성자 자동 생성하는 어노테이션
@EntityListeners(AuditingEntityListener.class) // 시간 확인을 위한 어노테이션
@Table(name = "USER")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long id; // 회원 고유 번호 (PK)

    @Column(name = "EMAIL", nullable = false, length = 100, unique = true)
    private String email; // ID로 쓸 email (중복 불가)

    @Column(name = "PASSWORD", length = 255)
    private String password; // 비밀번호

    @Column(name = "NICKNAME", nullable = false, length = 50)
    private String nickname; // 닉네임

    @Column(name = "PROVIDER", nullable = false, length = 20)
    private String provider; // 가입 출처 구분
    // LOCAL : 일반  / GOOGLE : 구글 연동 로그인

    @Column(name = "OAUTH_ID", length = 255)
    private String oauthId;

    @CreatedDate
    @Column(name = "JOINED_AT", updatable = false)
    private LocalDateTime joinedAt;

    // 나중에 데이터를 쉽게 넣기 위한 생성자
    @Builder
    public Member(String email, String password, String nickname, String provider, String oauthId) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.provider = provider;
        this.oauthId = oauthId;
    }
}