package com.sports.dto;

import lombok.Data;

/**
 * 用户信息更新请求DTO
 */
@Data
public class UserUpdateRequest {
    private String nickname;
    private String email;
    private String avatar;
}
