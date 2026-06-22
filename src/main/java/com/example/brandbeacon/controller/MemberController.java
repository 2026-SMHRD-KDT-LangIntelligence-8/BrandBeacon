package com.example.brandbeacon.controller;

import com.example.brandbeacon.domain.Member;
import com.example.brandbeacon.dto.JoinRequest;
import com.example.brandbeacon.dto.MemberUpdateRequest;
import com.example.brandbeacon.repository.MemberRepository;
import com.example.brandbeacon.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    // 1. 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<String> checkEmail(@RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("이메일을 입력해주세요.");
        }
        boolean exists = memberRepository.findByEmail(email.trim()).isPresent();
        if (exists) {
            return ResponseEntity.status(409).body("이미 사용 중인 이메일입니다.");
        }
        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }

    // 2. 일반 회원가입 요청 받기
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

    // 2. 일반 로그인 요청 받기
    @PostMapping("/login")
    public ResponseEntity<String> loginLocal(@RequestBody JoinRequest request, HttpServletRequest httpRequest) {
        try {
            // Service 객체로 넘겨 로그인 검사
            Member loginMember = memberService.loginLocal(request.getEmail(), request.getPassword());

            // 세션을 생성하여 로그인 성공 상태 유지
            HttpSession session = httpRequest.getSession();
            session.setAttribute("loginUserId", loginMember.getId());

            // 에러 없이 통과했다면 성공!
            return ResponseEntity.ok("로그인 성공!");

        } catch (IllegalArgumentException e) {
            // 비밀번호가 틀리거나 없는 이메일이면 에러 메시지 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. 회원 정보 수정 (PUT 요청)
    @PutMapping("/me")
    public ResponseEntity<String> updateMember(@RequestBody MemberUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            // 세션 확인 (로그인 여부 깐깐하게 체크)
            HttpSession session = httpRequest.getSession(false);
            if (session == null || session.getAttribute("loginUserId") == null) {
                return ResponseEntity.status(401).body("로그인이 필요한 서비스입니다.");
            }
            Long userId = (Long) session.getAttribute("loginUserId");

            // 서비스로 수정 데이터 전달
            memberService.updateMember(userId, request.getCurrentPassword(), request.getNickname(), request.getPassword());
            return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. 회원 탈퇴 (DELETE 요청)
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMember(HttpServletRequest httpRequest) {
        try {
            // 세션 확인
            HttpSession session = httpRequest.getSession(false);
            if (session == null || session.getAttribute("loginUserId") == null) {
                return ResponseEntity.status(401).body("로그인이 필요한 서비스입니다.");
            }
            Long userId = (Long) session.getAttribute("loginUserId");

            // 서비스로 탈퇴 지시
            memberService.deleteMember(userId);

            // 회원이 탈퇴했으니 남아있는 세션도 즉시 파기!
            session.invalidate();

            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
