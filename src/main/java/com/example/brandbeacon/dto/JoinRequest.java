package com.example.brandbeacon.dto;

import lombok.Getter;

@Getter
public class JoinRequest {
    private String email;
    private String password;
    private String nickname;
}