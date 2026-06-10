package com.example.brandbeacon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 비밀번호 암호화에 사용할 인코더 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. CSRF (보안 해킹 방지) 기능 끄기
                .csrf(csrf -> csrf.disable())

                // 3. 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 지금은 모든 요청 일단 통과!
                )

                // 4. 구글 소셜 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("http://localhost:3000/main", true)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. 허용할 프론트엔드 주소
        // 실제 배포용 주소 여기에 추가하기!
        // React나 Vue의 기본 로컬 주소 -> 3000번 포트 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));

        // 2. 허용할 HTTP 메서드 (조회, 생성, 수정, 삭제 다 허용!)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 3. 허용할 헤더
        // 프론트가 보내는 모든 형태의 데이터와 토큰을 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 4. 내 서버가 응답할 때 프론트가 데이터를 자바스크립트로 읽을 수 있게 허락
        // -> 보안상 매우 중요
        configuration.setAllowCredentials(true);

        // 5. 이 설정을 우리 서버의 모든 API 주소("/**")에 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}