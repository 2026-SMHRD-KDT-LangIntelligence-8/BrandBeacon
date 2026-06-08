package com.example.brandbeacon.controller;

import com.example.brandbeacon.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    // Service 클래스 불러오기
    private final MemberService memberService;

    // 1. 일반 회원가입 요청 받기
    @PostMapping("/join")
    public ResponseEntity<String> joinLocal(@RequestBody JoinRequest request) {
        try {
            // 받은 데이터를 Service 객체로 넘겨 가입 처리
            memberService.joinLocal(request.getEmail(), request.getPassword(), request.getNickname());

            // 성공하면 환영 인사 반환
            return ResponseEntity.ok("회원가입이 완료되었습니다. 환영합니다!");

        } catch (IllegalArgumentException e) {
            // 이미 가입된 email이라면, 여기서 잡아서 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 사용자가 입력한 회원 정보 저장하는 곳
    @lombok.Getter
    public static class JoinRequest {
        private String email;
        private String password;
        private String nickname;
    }
}
