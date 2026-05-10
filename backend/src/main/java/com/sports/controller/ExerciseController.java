package com.sports.controller;

import com.sports.common.ApiResponse;
import com.sports.dto.ExerciseRequest;
import com.sports.dto.ExerciseResponse;
import com.sports.entity.ExerciseType;
import com.sports.entity.User;
import com.sports.service.ExerciseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 运动记录控制器
 */
@RestController
@RequestMapping("/api")
public class ExerciseController {
    
    @Autowired
    private ExerciseService exerciseService;
    
    @GetMapping("/exercise-types")
    public ResponseEntity<ApiResponse<List<ExerciseType>>> getExerciseTypes() {
        List<ExerciseType> types = exerciseService.getAllExerciseTypes();
        return ResponseEntity.ok(ApiResponse.success(types));
    }
    
    @GetMapping("/exercises")
    public ResponseEntity<ApiResponse<Page<ExerciseResponse>>> getExercises(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ExerciseResponse> exercises = exerciseService.getExercisesByUserId(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(exercises));
    }
    
    @GetMapping("/exercises/range")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> getExercisesByDateRange(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ExerciseResponse> exercises = exerciseService.getExercisesByDateRange(user.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(exercises));
    }
    
    @PostMapping("/exercises")
    public ResponseEntity<ApiResponse<ExerciseResponse>> createExercise(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ExerciseRequest request) {
        ExerciseResponse exercise = exerciseService.createExercise(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("运动记录添加成功", exercise));
    }
    
    @PutMapping("/exercises/{id}")
    public ResponseEntity<ApiResponse<ExerciseResponse>> updateExercise(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody ExerciseRequest request) {
        ExerciseResponse exercise = exerciseService.updateExercise(user.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", exercise));
    }
    
    @DeleteMapping("/exercises/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExercise(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        exerciseService.deleteExercise(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("删除成功"));
    }
}
