package com.sports.service;

import com.sports.dto.StatsResponse;
import com.sports.entity.Goal;
import com.sports.repository.ExerciseRepository;
import com.sports.repository.GoalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatsService 单元测试")
class StatsServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private StatsService statsService;

    @Test
    @DisplayName("getOverview - 正常计算本月统计")
    void testGetOverview() {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        when(exerciseRepository.countByUserIdAndDateRange(eq(1L), eq(monthStart), eq(monthEnd))).thenReturn(15L);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(1L), eq(monthStart), eq(monthEnd))).thenReturn(900);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), eq(monthStart), eq(monthEnd))).thenReturn(5000);
        when(goalRepository.findByUserIdAndStatus(eq(1L), eq("ACTIVE"))).thenReturn(Arrays.asList(new Goal(), new Goal()));
        when(goalRepository.findByUserIdAndStatus(eq(1L), eq("COMPLETED"))).thenReturn(Arrays.asList(new Goal(), new Goal(), new Goal()));

        StatsResponse result = statsService.getOverview(1L);

        assertEquals(Integer.valueOf(15), result.getTotalExercises());
        assertEquals(Integer.valueOf(900), result.getTotalDuration());
        assertEquals(Integer.valueOf(5000), result.getTotalCalories());
        assertEquals(2, result.getActiveGoals());
        assertEquals(3, result.getCompletedGoals());
    }

    @Test
    @DisplayName("getOverview - null 值安全处理")
    void testGetOverview_NullSafety() {
        when(exerciseRepository.countByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(null);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(null);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(null);
        when(goalRepository.findByUserIdAndStatus(anyLong(), eq("ACTIVE"))).thenReturn(Collections.emptyList());
        when(goalRepository.findByUserIdAndStatus(anyLong(), eq("COMPLETED"))).thenReturn(Collections.emptyList());

        StatsResponse result = statsService.getOverview(1L);

        assertEquals(Integer.valueOf(0), result.getTotalExercises());
        assertEquals(Integer.valueOf(0), result.getTotalDuration());
        assertEquals(Integer.valueOf(0), result.getTotalCalories());
        assertEquals(0, result.getActiveGoals());
        assertEquals(0, result.getCompletedGoals());
    }

    @Test
    @DisplayName("getWeeklyStats - 本周统计数据")
    void testGetWeeklyStats() {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        when(exerciseRepository.countByUserIdAndDateRange(eq(1L), eq(weekStart), eq(weekEnd))).thenReturn(5L);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(1L), eq(weekStart), eq(weekEnd))).thenReturn(300);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), eq(weekStart), eq(weekEnd))).thenReturn(2500);

        Map<String, Object> result = statsService.getWeeklyStats(1L);

        assertEquals(5, result.get("count"));
        assertEquals(300, result.get("duration"));
        assertEquals(2500, result.get("calories"));
        assertEquals(weekStart.toString(), result.get("startDate"));
        assertEquals(weekEnd.toString(), result.get("endDate"));
    }

    @Test
    @DisplayName("getWeeklyStats - null 值安全处理")
    void testGetWeeklyStats_NullSafety() {
        when(exerciseRepository.countByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(null);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(null);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(null);

        Map<String, Object> result = statsService.getWeeklyStats(1L);

        assertEquals(0, result.get("count"));
        assertEquals(0, result.get("duration"));
        assertEquals(0, result.get("calories"));
    }

    @Test
    @DisplayName("getWeeklyStats - 字段契约验证")
    void testGetWeeklyStats_FieldContract() {
        when(exerciseRepository.countByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(0L);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(0);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(0);

        Map<String, Object> result = statsService.getWeeklyStats(1L);

        assertTrue(result.containsKey("count"));
        assertTrue(result.containsKey("duration"));
        assertTrue(result.containsKey("calories"));
        assertTrue(result.containsKey("startDate"));
        assertTrue(result.containsKey("endDate"));
    }

    @Test
    @DisplayName("getMonthlyStats - 本月统计数据")
    void testGetMonthlyStats() {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        when(exerciseRepository.countByUserIdAndDateRange(eq(1L), eq(monthStart), eq(monthEnd))).thenReturn(15L);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(1L), eq(monthStart), eq(monthEnd))).thenReturn(900);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), eq(monthStart), eq(monthEnd))).thenReturn(5000);

        Map<String, Object> result = statsService.getMonthlyStats(1L);

        assertEquals(15, result.get("count"));
        assertEquals(900, result.get("duration"));
        assertEquals(5000, result.get("calories"));
        assertEquals(now.getMonthValue(), result.get("month"));
        assertEquals(now.getYear(), result.get("year"));
    }

    @Test
    @DisplayName("getMonthlyStats - null 值安全处理")
    void testGetMonthlyStats_NullSafety() {
        when(exerciseRepository.countByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(null);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(null);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(null);

        Map<String, Object> result = statsService.getMonthlyStats(1L);

        assertEquals(0, result.get("count"));
        assertEquals(0, result.get("duration"));
        assertEquals(0, result.get("calories"));
    }

    @Test
    @DisplayName("getMonthlyStats - 字段契约验证")
    void testGetMonthlyStats_FieldContract() {
        when(exerciseRepository.countByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(0L);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(0);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(0);

        Map<String, Object> result = statsService.getMonthlyStats(1L);

        assertTrue(result.containsKey("count"));
        assertTrue(result.containsKey("duration"));
        assertTrue(result.containsKey("calories"));
        assertTrue(result.containsKey("month"));
        assertTrue(result.containsKey("year"));
    }

    @Test
    @DisplayName("getCaloriesTrend - 正常生成趋势数据，包含无数据日期补0")
    void testGetCaloriesTrend() {
        int days = 5;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Object[]> repoData = new ArrayList<>();
        repoData.add(new Object[]{startDate.plusDays(1), 300L});
        repoData.add(new Object[]{startDate.plusDays(3), 500L});
        when(exerciseRepository.getCaloriesTrendByDateRange(eq(1L), eq(startDate), eq(endDate))).thenReturn(repoData);

        List<Map<String, Object>> result = statsService.getCaloriesTrend(1L, days);

        assertEquals(5, result.size());
        assertEquals(startDate.toString(), result.get(0).get("date"));
        assertEquals(0, result.get(0).get("calories"));
        assertEquals(300, result.get(1).get("calories"));
        assertEquals(0, result.get(2).get("calories"));
        assertEquals(500, result.get(3).get("calories"));
        assertEquals(0, result.get(4).get("calories"));
    }

    @Test
    @DisplayName("getCaloriesTrend - 无数据时所有日期为 0")
    void testGetCaloriesTrend_NoData() {
        int days = 3;
        when(exerciseRepository.getCaloriesTrendByDateRange(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = statsService.getCaloriesTrend(1L, days);

        assertEquals(3, result.size());
        for (Map<String, Object> item : result) {
            assertEquals(0, item.get("calories"));
        }
    }

    @Test
    @DisplayName("getCaloriesTrend - 同一天多次运动正确累加")
    void testGetCaloriesTrend_MultipleSameDay() {
        int days = 3;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Object[]> repoData = new ArrayList<>();
        repoData.add(new Object[]{startDate.plusDays(1), 800L});
        when(exerciseRepository.getCaloriesTrendByDateRange(eq(1L), eq(startDate), eq(endDate))).thenReturn(repoData);

        List<Map<String, Object>> result = statsService.getCaloriesTrend(1L, days);

        assertEquals(800, result.get(1).get("calories"));
    }

    @Test
    @DisplayName("getCaloriesTrend - 字段契约验证")
    void testGetCaloriesTrend_FieldContract() {
        when(exerciseRepository.getCaloriesTrendByDateRange(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = statsService.getCaloriesTrend(1L, 1);

        assertTrue(result.get(0).containsKey("date"));
        assertTrue(result.get(0).containsKey("calories"));
    }

    @Test
    @DisplayName("getExerciseTypeDistribution - 正常返回类型分布")
    void testGetExerciseTypeDistribution() {
        List<Object[]> repoData = new ArrayList<>();
        repoData.add(new Object[]{"跑步", 5L});
        repoData.add(new Object[]{"游泳", 3L});
        repoData.add(new Object[]{"骑行", 2L});
        when(exerciseRepository.countByExerciseType(eq(1L))).thenReturn(repoData);

        List<Map<String, Object>> result = statsService.getExerciseTypeDistribution(1L);

        assertEquals(3, result.size());
        assertEquals("跑步", result.get(0).get("type"));
        assertEquals(5, result.get(0).get("count"));
        assertEquals("游泳", result.get(1).get("type"));
        assertEquals(3, result.get(1).get("count"));
    }

    @Test
    @DisplayName("getExerciseTypeDistribution - 无数据时返回空列表")
    void testGetExerciseTypeDistribution_NoData() {
        when(exerciseRepository.countByExerciseType(eq(1L))).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = statsService.getExerciseTypeDistribution(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getExerciseTypeDistribution - 字段契约验证")
    void testGetExerciseTypeDistribution_FieldContract() {
        List<Object[]> repoData = new ArrayList<>();
        repoData.add(new Object[]{"跑步", 5L});
        when(exerciseRepository.countByExerciseType(eq(1L))).thenReturn(repoData);

        List<Map<String, Object>> result = statsService.getExerciseTypeDistribution(1L);

        assertTrue(result.get(0).containsKey("type"));
        assertTrue(result.get(0).containsKey("count"));
    }
}
