package com.example.brandbeacon.domain; // 👈 브랜드비컨으로 이름 바꿨습니다!

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor // 기본 생성자 자동 생성하는 어노테이션
@EntityListeners(AuditingEntityListener.class) // 시간 확인을 위한 어노테이션
@Table(name = "user")
public class Member {

    // (질문자님이 작성하신 내용과 100% 동일합니다)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 회원 고유 번호 (PK)

    @Column(nullable = false, unique = true)
    private String email; // ID로 쓸 email (중복 불가)

    private String password; // 비밀번호

    @Column(nullable = false)
    private String nickname; // 닉네임

    @Column(nullable = false)
    private String provider; // 가입 출처 구분
    // LOCAL : 일반  / GOOGLE : 구글 연동 로그인

    // 나중에 데이터를 쉽게 넣기 위한 생성자
    public Member(String email, String password, String nickname, String provider) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.provider = provider;
    }
}