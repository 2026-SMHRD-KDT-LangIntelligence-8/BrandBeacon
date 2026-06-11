package com.example.brandbeacon.service;

import com.example.brandbeacon.domain.Member;
import com.example.brandbeacon.repository.MemberRepository; // 본인 프로젝트 패키지에 맞게 수정
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 구글이 준 유저 정보 꺼내기
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String sub = (String) attributes.get("sub"); // 구글 고유 ID

        // DB에 이메일이 있는지 확인하고 없으면 신규 가입(저장) 처리
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .email(email)
                            .nickname(name)
                            .oauthId(sub)
                            .password(null) // 소셜 로그인은 비밀번호가 없음
                            .build();
                    return memberRepository.save(newMember);
                });

        return new DefaultOAuth2User(
                Collections.emptyList(),
                attributes,
                "email" // 이메일을 고유 식별자로 지정
        );
    }
}
