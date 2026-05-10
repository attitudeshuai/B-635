package com.sports.controller;

import com.sports.common.ApiResponse;
import com.sports.dto.LoginRequest;
import com.sports.dto.LoginResponse;
import com.sports.dto.RegisterRequest;
import com.sports.entity.User;
import com.sports.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        user.setPassword(null); // 不返回密码
        return ResponseEntity.ok(ApiResponse.success("注册成功", user));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }
}
