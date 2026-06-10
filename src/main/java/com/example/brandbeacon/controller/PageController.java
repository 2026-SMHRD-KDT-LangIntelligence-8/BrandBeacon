package com.example.brandbeacon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String landing() {
        System.out.println("✨ 랜딩 페이지(메인) 접속 요청이 들어왔습니다!");
        return "pages/landing";
    }

    @GetMapping("/login")
    public String login() {
        System.out.println("🔑 로그인 페이지 접속 요청이 들어왔습니다!");
        return "pages/login";
    }

    @GetMapping("/signup")
    public String signup() {
        System.out.println("📝 회원가입 페이지 접속 요청이 들어왔습니다!");
        return "pages/signup";
    }

    @GetMapping("/service")
    public String service() {
        System.out.println("📖 서비스 소개 페이지 접속 요청이 들어왔습니다!");
        return "pages/service";
    }

    @GetMapping("/archive")
    public String archive() {
        System.out.println("📁 프로젝트 저장소 페이지 접속 요청이 들어왔습니다!");
        return "pages/archive";
    }

    @GetMapping("/profileEdit")
    public String profileEdit() {
        System.out.println("👤 회원정보 수정 페이지 접속 요청이 들어왔습니다!");
        return "pages/profileEdit";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        System.out.println("📊 대시보드 페이지 접속 요청이 들어왔습니다!");
        return "pages/dashboard";
    }

    @GetMapping("/moodboard")
    public String moodboard() {
        System.out.println("🎨 무드보드 페이지 접속 요청이 들어왔습니다!");
        return "pages/moodboard";
    }

    @GetMapping("/reference")
    public String reference() {
        System.out.println("📚 레퍼런스 페이지 접속 요청이 들어왔습니다!");
        return "pages/reference";
    }

    @GetMapping("/brandsync")
    public String brandsync() {
        System.out.println("🔄 브랜드 싱크 페이지 접속 요청이 들어왔습니다!");
        return "pages/brandsync";
    }
}