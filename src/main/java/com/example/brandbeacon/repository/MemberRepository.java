package com.example.brandbeacon.repository;

import com.example.brandbeacon.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이메일로 회원 정보 찾기 (로그인, 중복검사 시 필수!)
    Optional<Member> findByEmail(String email);
}