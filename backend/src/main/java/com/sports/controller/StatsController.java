package com.sports.controller;

import com.sports.common.ApiResponse;
import com.sports.dto.StatsResponse;
import com.sports.entity.User;
import com.sports.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {
    
    @Autowired
    private StatsService statsService;
    
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<StatsResponse>> getOverview(@AuthenticationPrincipal User user) {
        StatsResponse stats = statsService.getOverview(user.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWeeklyStats(@AuthenticationPrincipal User user) {
        Map<String, Object> stats = statsService.getWeeklyStats(user.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyStats(@AuthenticationPrincipal User user) {
        Map<String, Object> stats = statsService.getMonthlyStats(user.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCaloriesTrend(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> trend = statsService.getCaloriesTrend(user.getId(), days);
        return ResponseEntity.ok(ApiResponse.success(trend));
    }
    
    @GetMapping("/distribution")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTypeDistribution(@AuthenticationPrincipal User user) {
        List<Map<String, Object>> distribution = statsService.getExerciseTypeDistribution(user.getId());
        return ResponseEntity.ok(ApiResponse.success(distribution));
    }
}
