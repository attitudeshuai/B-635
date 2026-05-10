package com.sports.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String nickname;
    private String email;
    private Long userId;
}
