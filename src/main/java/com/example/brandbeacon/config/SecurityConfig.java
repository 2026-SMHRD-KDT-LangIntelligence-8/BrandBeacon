package com.example.brandbeacon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 프리패스 경로 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll() // 메인 화면 -> 로그인 없이 통과
                        .anyRequest().authenticated() // 그 외의 다른 모든 페이지는 로그인해야 이용가능
                )
                // 2. 구글 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/") // 로그인 성공 -> 다시 메인 화면으로 돌려보내기
                );

        return http.build();
    }
}
