package com.sports.controller;

import com.sports.common.ApiResponse;
import com.sports.dto.GoalRequest;
import com.sports.dto.GoalResponse;
import com.sports.entity.User;
import com.sports.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 目标控制器
 */
@RestController
@RequestMapping("/api/goals")
public class GoalController {
    
    @Autowired
    private GoalService goalService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoals(@AuthenticationPrincipal User user) {
        List<GoalResponse> goals = goalService.getGoalsByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success(goals));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody GoalRequest request) {
        GoalResponse goal = goalService.createGoal(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("目标创建成功", goal));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody GoalRequest request) {
        GoalResponse goal = goalService.updateGoal(user.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", goal));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        goalService.deleteGoal(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("删除成功"));
    }
}
