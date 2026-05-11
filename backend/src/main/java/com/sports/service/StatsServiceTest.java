package com.sports.service;

import com.sports.dto.StatsResponse;
import com.sports.entity.Goal;
import com.sports.repository.ExerciseRepository;
import com.sports.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private StatsService statsService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    void getOverview_ShouldReturnCorrectStats() {
        when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(10L);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(300);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(2500);
        when(goalRepository.findByUserIdAndStatus(userId, "ACTIVE")).thenReturn(Arrays.asList(new Goal(), new Goal()));
        when(goalRepository.findByUserIdAndStatus(userId, "COMPLETED")).thenReturn(Arrays.asList(new Goal()));

        StatsResponse result = statsService.getOverview(userId);

        assertEquals(10, result.getTotalExercises());
        assertEquals(300, result.getTotalDuration());
        assertEquals(2500, result.getTotalCalories());
        assertEquals(2, result.getActiveGoals());
        assertEquals(1, result.getCompletedGoals());
    }

    @Test
    void getOverview_WithNullValues_ShouldHandleGracefully() {
        when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
        when(goalRepository.findByUserIdAndStatus(userId, "ACTIVE")).thenReturn(Collections.emptyList());
        when(goalRepository.findByUserIdAndStatus(userId, "COMPLETED")).thenReturn(Collections.emptyList());

        StatsResponse result = statsService.getOverview(userId);

        assertEquals(0, result.getTotalExercises());
        assertEquals(0, result.getTotalDuration());
        assertEquals(0, result.getTotalCalories());
        assertEquals(0, result.getActiveGoals());
        assertEquals(0, result.getCompletedGoals());
    }

    @Test
    void getWeeklyStats_ShouldReturnWeeklyData() {
        when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(5L);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(150);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(1200);

        Map<String, Object> result = statsService.getWeeklyStats(userId);

        assertEquals(5, result.get("count"));
        assertEquals(150, result.get("duration"));
        assertEquals(1200, result.get("calories"));
        assertNotNull(result.get("startDate"));
        assertNotNull(result.get("endDate"));
    }

    @Test
    void getWeeklyStats_WithNullValues_ShouldReturnZeros() {
        when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);

        Map<String, Object> result = statsService.getWeeklyStats(userId);

        assertEquals(0, result.get("count"));
        assertEquals(0, result.get("duration"));
        assertEquals(0, result.get("calories"));
    }

    @Test
    void getMonthlyStats_ShouldReturnMonthlyData() {
        when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(20L);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(600);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(5000);

        Map<String, Object> result = statsService.getMonthlyStats(userId);

        assertEquals(20, result.get("count"));
        assertEquals(600, result.get("duration"));
        assertEquals(5000, result.get("calories"));
        assertNotNull(result.get("month"));
        assertNotNull(result.get("year"));
    }

    @Test
    void getMonthlyStats_WithNullValues_ShouldReturnZeros() {
        when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
        when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);

        Map<String, Object> result = statsService.getMonthlyStats(userId);

        assertEquals(0, result.get("count"));
        assertEquals(0, result.get("duration"));
        assertEquals(0, result.get("calories"));
    }

    @Test
    void getCaloriesTrend_ShouldReturnCompleteDateSequence() {
        int days = 7;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Object[]> mockData = new ArrayList<>();
        mockData.add(new Object[]{startDate.plusDays(1), 200L});
        mockData.add(new Object[]{startDate.plusDays(3), 300L});

        when(exerciseRepository.getCaloriesTrendByDateRange(eq(userId), any(), any())).thenReturn(mockData);

        List<Map<String, Object>> result = statsService.getCaloriesTrend(userId, days);

        assertEquals(days, result.size());
        assertEquals(200, result.get(1).get("calories"));
        assertEquals(300, result.get(3).get("calories"));
        assertEquals(0, result.get(0).get("calories"));
        assertNotNull(result.get(0).get("date"));
    }

    @Test
    void getCaloriesTrend_WithEmptyData_ShouldReturnAllZeros() {
        int days = 5;
        when(exerciseRepository.getCaloriesTrendByDateRange(eq(userId), any(), any())).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = statsService.getCaloriesTrend(userId, days);

        assertEquals(days, result.size());
        result.forEach(item -> assertEquals(0, item.get("calories")));
    }

    @Test
    void getCaloriesTrend_ShouldHandleNullCalories() {
        int days = 3;
        List<Object[]> mockData = new ArrayList<>();
        mockData.add(new Object[]{LocalDate.now(), null});

        when(exerciseRepository.getCaloriesTrendByDateRange(eq(userId), any(), any())).thenReturn(mockData);

        List<Map<String, Object>> result = statsService.getCaloriesTrend(userId, days);

        assertEquals(0, result.get(2).get("calories"));
    }

    @Test
    void getExerciseTypeDistribution_ShouldReturnCorrectData() {
        List<Object[]> mockData = new ArrayList<>();
        mockData.add(new Object[]{"Running", 10L});
        mockData.add(new Object[]{"Swimming", 5L});
        mockData.add(new Object[]{"Cycling", 8L});

        when(exerciseRepository.countByExerciseType(userId)).thenReturn(mockData);

        List<Map<String, Object>> result = statsService.getExerciseTypeDistribution(userId);

        assertEquals(3, result.size());
        assertEquals("Running", result.get(0).get("type"));
        assertEquals(10, result.get(0).get("count"));
        assertEquals("Swimming", result.get(1).get("type"));
        assertEquals(5, result.get(1).get("count"));
        assertEquals("Cycling", result.get(2).get("type"));
        assertEquals(8, result.get(2).get("count"));
    }

    @Test
    void getExerciseTypeDistribution_WithEmptyData_ShouldReturnEmptyList() {
        when(exerciseRepository.countByExerciseType(userId)).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = statsService.getExerciseTypeDistribution(userId);

        assertTrue(result.isEmpty());
    }
}
