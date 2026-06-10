package com.example.brandbeacon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // 스프링 서버와 파이썬 서버를 연결시키기 위함!
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}