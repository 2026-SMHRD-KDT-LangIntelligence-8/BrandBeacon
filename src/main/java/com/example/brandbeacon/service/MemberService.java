package com.example.brandbeacon.service;

import com.example.brandbeacon.domain.Member;
import com.example.brandbeacon.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // 서비스 파일임을 알리는 어노테이션
@RequiredArgsConstructor // Repository 가져오는 어노테이션
public class MemberService {

    // Repository 불러오기
    private final MemberRepository memberRepository;

    // 1. 일반 회원가입 (email, 비밀번호 직접 입력)
    public Member joinLocal(String email, String password, String nickname) {
        // email 중복 확인 코드
        if (memberRepository.findByEmail(email).isPresent()) {
            // 중복이라면 중복 에러 알림 발생
            throw new IllegalArgumentException("이미 가입된 이메일입니다!");
        }

        // 중복이 아니라면 회원 가입 가능, DB에 저장
        Member newMember = new Member(email, password, nickname, "LOCAL");
        return memberRepository.save(newMember);
    }


    // 2. Google email 연동 가입
    @Transactional
    public Member loginOrjoinGoogle(String email, String nickname) {
        // email로 회원 찾기
        return memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 만약 찾지 못했다면, 아래 코드 실행해서 새로 가입할 수 있도록!
                    Member newGoogleMember = new Member(email, null, nickname, "GOOGLE");
                    return memberRepository.save(newGoogleMember);
                });
    }

}
