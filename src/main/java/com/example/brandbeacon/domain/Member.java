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

    @Column(name = "OAUTH_ID", length = 255)
    private String oauthId; // 구글 로그인 고유 ID

    @CreatedDate
    @Column(name = "JOINED_AT", updatable = false)
    private LocalDateTime joinedAt; // 가입 일자





    // 나중에 데이터를 쉽게 넣기 위한 생성자 (@Builder 적용)
    @Builder
    public Member(String email, String password, String nickname, String oauthId) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.oauthId = oauthId;
    }


    // 마이페이지 기능: 회원 정보(닉네임, 비밀번호) 수정을 위한 메서드
    public void updateInfo(String nickname, String encryptedPassword) {
        // 닉네임이 비어있지 않을 때만 수정
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        // 비밀번호 -> 새로 입력했을 때만 수정 (입력 안 했으면 기존 비밀번호 유지)
        if (encryptedPassword != null && !encryptedPassword.isBlank()) {
            this.password = encryptedPassword;
        }
    }
}