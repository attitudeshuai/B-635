package com.sports.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 统计数据响应DTO
 */
@Data
public class StatsResponse {
    private Integer totalExercises;
    private Integer totalDuration;
    private Integer totalCalories;
    private Integer activeGoals;
    private Integer completedGoals;
    private List<Map<String, Object>> caloriesTrend;
    private List<Map<String, Object>> exerciseTypeDistribution;
    private List<Map<String, Object>> weeklyStats;
}
