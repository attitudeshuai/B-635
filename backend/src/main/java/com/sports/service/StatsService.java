package com.sports.service;

import com.sports.dto.StatsResponse;
import com.sports.repository.ExerciseRepository;
import com.sports.repository.GoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * 统计服务
 */
@Service
public class StatsService {
    
    @Autowired
    private ExerciseRepository exerciseRepository;
    
    @Autowired
    private GoalRepository goalRepository;
    
    public StatsResponse getOverview(Long userId) {
        StatsResponse response = new StatsResponse();
        
        // 本月统计
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        
        // 总运动次数
        Long totalCount = exerciseRepository.countByUserIdAndDateRange(userId, monthStart, monthEnd);
        response.setTotalExercises(totalCount != null ? totalCount.intValue() : 0);
        
        // 总时长
        Integer totalDuration = exerciseRepository.sumDurationByUserIdAndDateRange(userId, monthStart, monthEnd);
        response.setTotalDuration(totalDuration != null ? totalDuration : 0);
        
        // 总卡路里
        Integer totalCalories = exerciseRepository.sumCaloriesByUserIdAndDateRange(userId, monthStart, monthEnd);
        response.setTotalCalories(totalCalories != null ? totalCalories : 0);
        
        // 目标统计
        int activeGoals = goalRepository.findByUserIdAndStatus(userId, "ACTIVE").size();
        int completedGoals = goalRepository.findByUserIdAndStatus(userId, "COMPLETED").size();
        response.setActiveGoals(activeGoals);
        response.setCompletedGoals(completedGoals);
        
        return response;
    }
    
    public Map<String, Object> getWeeklyStats(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        
        Map<String, Object> stats = new HashMap<>();
        
        Long count = exerciseRepository.countByUserIdAndDateRange(userId, weekStart, weekEnd);
        Integer duration = exerciseRepository.sumDurationByUserIdAndDateRange(userId, weekStart, weekEnd);
        Integer calories = exerciseRepository.sumCaloriesByUserIdAndDateRange(userId, weekStart, weekEnd);
        
        stats.put("count", count != null ? count.intValue() : 0);
        stats.put("duration", duration != null ? duration : 0);
        stats.put("calories", calories != null ? calories : 0);
        stats.put("startDate", weekStart.toString());
        stats.put("endDate", weekEnd.toString());
        
        return stats;
    }
    
    public Map<String, Object> getMonthlyStats(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        
        Map<String, Object> stats = new HashMap<>();
        
        Long count = exerciseRepository.countByUserIdAndDateRange(userId, monthStart, monthEnd);
        Integer duration = exerciseRepository.sumDurationByUserIdAndDateRange(userId, monthStart, monthEnd);
        Integer calories = exerciseRepository.sumCaloriesByUserIdAndDateRange(userId, monthStart, monthEnd);
        
        stats.put("count", count != null ? count.intValue() : 0);
        stats.put("duration", duration != null ? duration : 0);
        stats.put("calories", calories != null ? calories : 0);
        stats.put("month", now.getMonthValue());
        stats.put("year", now.getYear());
        
        return stats;
    }
    
    public List<Map<String, Object>> getCaloriesTrend(Long userId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        List<Object[]> data = exerciseRepository.getCaloriesTrendByDateRange(userId, startDate, endDate);
        
        // 创建日期到卡路里的映射
        Map<LocalDate, Integer> caloriesMap = new HashMap<>();
        for (Object[] row : data) {
            LocalDate date = (LocalDate) row[0];
            Long calories = (Long) row[1];
            caloriesMap.put(date, calories != null ? calories.intValue() : 0);
        }
        
        // 生成完整的日期序列
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("calories", caloriesMap.getOrDefault(date, 0));
            result.add(item);
        }
        
        return result;
    }
    
    public List<Map<String, Object>> getExerciseTypeDistribution(Long userId) {
        List<Object[]> data = exerciseRepository.countByExerciseType(userId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : data) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", row[0]);
            item.put("count", ((Long) row[1]).intValue());
            result.add(item);
        }
        
        return result;
    }
}
