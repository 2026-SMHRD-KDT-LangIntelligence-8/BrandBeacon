package com.example.brandbeacon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequest {
    private String nickname; // 변경할 닉네임
    private String password; // 변경할 비밀번호 (변경하지 않을 거라면 프론트에서 비워두고 보냄)
}
