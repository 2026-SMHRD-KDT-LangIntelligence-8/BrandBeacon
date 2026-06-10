package com.example.brandbeacon.service;

import com.example.brandbeacon.domain.User;
import com.example.brandbeacon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // 1. 일반 회원가입
    @Transactional
    public User joinLocal(String email, String password, String nickname) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다!");
        }

        User newUser = User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .build();

        return userRepository.save(newUser);
    }

    // 2. Google 연동 가입 (Provider 필드 제외, OauthId 활용)
    @Transactional
    public User loginOrJoinGoogle(String email, String nickname, String oauthId) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // DB 명세서에 맞춰 OauthId를 저장합니다.
                    User newGoogleUser = User.builder()
                            .email(email)
                            .nickname(nickname)
                            .oauthId(oauthId)
                            .build();

                    return userRepository.save(newGoogleUser);
                });
    }
}