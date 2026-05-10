package com.sports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 密码修改请求DTO
 */
@Data
public class PasswordChangeRequest {
    
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度为6-100个字符")
    private String newPassword;
}
