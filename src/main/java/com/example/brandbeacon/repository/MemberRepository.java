package com.example.brandbeacon.repository;

import com.example.brandbeacon.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 구글 로그인 & 아이디 중복 검사를 위함!
    Optional<Member> findByEmail(String email);

}
