package com.example.brandbeacon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // ⭐️ 빈의 이름을 "aiRestTemplate"으로 지정합니다.
    @Bean(name = "aiRestTemplate")
    public RestTemplate aiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(10000); // 10초
        factory.setReadTimeout(60000);    // 60초 (AI 전용 넉넉한 타임아웃)

        return new RestTemplate(factory);
    }
}