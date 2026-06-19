package com.example.brandbeacon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequest {
    private String nickname;
    private String currentPassword; // 현재 비밀번호 (본인 확인용)
    private String password;        // 새로운 비밀번호
}
