package com.example.brandbeacon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JoinRequest {
    private String email;
    private String password;
    private String nickname;
}