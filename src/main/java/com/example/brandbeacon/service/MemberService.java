package com.example.brandbeacon.service;

import com.example.brandbeacon.domain.Member;
import com.example.brandbeacon.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // 서비스 파일임을 알리는 어노테이션
@RequiredArgsConstructor // Repository 가져오는 어노테이션
public class MemberService {

    // Repository 불러오기
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. 일반 회원가입 (email, 비밀번호 직접 입력)
    @Transactional
    public Member joinLocal(String email, String password, String nickname) {
        // email 중복 확인 코드
        if (memberRepository.findByEmail(email).isPresent()) {
            // 중복이라면 중복 에러 알림 발생
            throw new IllegalArgumentException("이미 가입된 이메일입니다!");
        }

        // 중복이 아니라면 회원 가입 가능, DB에 저장
        Member newMember = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password)) // 비밀번호 암호화 처리
                .nickname(nickname)
                .build();

        return memberRepository.save(newMember);
    }

    // 2. Google email 연동 가입
    @Transactional
    public Member loginOrjoinGoogle(String email, String nickname, String oauthId) {
        // email로 회원 찾기
        return memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 만약 찾지 못했다면, 아래 코드 실행해서 새로 가입할 수 있도록!
                    Member newGoogleMember = Member.builder()
                            .email(email)
                            .nickname(nickname)
                            .oauthId(oauthId)
                            .build();

                    return memberRepository.save(newGoogleMember);
                });
    }

    // 3. 일반 로그인 요청 받기
    @Transactional(readOnly = true)
    public Member loginLocal(String email, String password) {
        // email로 회원 찾기
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 비밀번호가 일치하는지 확인
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return member;
    }

    // 4. 회원 정보 수정 로직
    @Transactional
    public void updateMember(Long userId, String newNickname, String newPassword) {
        // DB에서 회원 찾아오기
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 새 비밀번호가 넘어왔다면 암호화, 아니면 null 처리
        String encryptedPassword = (newPassword != null && !newPassword.isBlank())
                ? passwordEncoder.encode(newPassword)
                : null;

        // 엔티티의 정보 업데이트
        // JPA의 영속성 컨텍스트 덕분에 save() 안 해도 자동 반영됨!
        member.updateInfo(newNickname, encryptedPassword);
    }

    // 5. 회원 탈퇴 로직
    @Transactional
    public void deleteMember(Long userId) {
        // 삭제할 회원 찾아오기
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 회원 정보를 DB에서 영구 삭제
        memberRepository.delete(member);
    }
}