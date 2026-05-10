package com.sports.controller;

import com.sports.common.ApiResponse;
import com.sports.dto.PasswordChangeRequest;
import com.sports.dto.UserUpdateRequest;
import com.sports.entity.User;
import com.sports.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(@AuthenticationPrincipal User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("createdAt", user.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
    
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UserUpdateRequest request) {
        User updatedUser = userService.updateUser(currentUser.getId(), request);
        updatedUser.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updatedUser));
    }
    
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功"));
    }
}
