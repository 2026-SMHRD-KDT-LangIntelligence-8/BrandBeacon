package com.example.brandbeacon.config;

import com.example.brandbeacon.domain.Member;
import com.example.brandbeacon.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // DB에서 로그인한 사용자 찾기
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 1. 일반 로그인과 똑같이 백엔드 세션에 로그인 ID 저장!
        HttpSession session = request.getSession();
        session.setAttribute("loginUserId", member.getId());

        // 2. 프론트에서 소셜 로그인 성공을 알아채도록 파라미터를 붙여 메인으로 리다이렉트!
        getRedirectStrategy().sendRedirect(request, response, "/?socialLogin=true");
    }
}