package com.example.brandbeacon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String landing() {
        System.out.println("✨ 랜딩 페이지(메인) 접속 요청이 들어왔습니다!"); // 👈 이 줄을 추가합니다.
        return "pages/landing";
    }

    @GetMapping("/login")
    public String login() {
        System.out.println("🔑 로그인 페이지 접속 요청이 들어왔습니다!"); // 👈 이 줄을 추가합니다.
        return "pages/login";
    }

    @GetMapping("/signup")
    public String signup() {
        System.out.println("📝 회원가입 페이지 접속 요청이 들어왔습니다!"); // 👈 이 줄을 추가합니다.
        return "pages/signup";
    }
}