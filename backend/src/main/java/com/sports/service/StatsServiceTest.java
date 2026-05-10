package com.sports.service;

import com.sports.dto.StatsResponse;
import com.sports.repository.ExerciseRepository;
import com.sports.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
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

    @Nested
    @DisplayName("getOverview")
    class GetOverviewTests {

        @Test
        @DisplayName("正常汇总月度运动统计和目标统计")
        void getOverview_success() {
            when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(10L);
            when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(300);
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(2500);
            when(goalRepository.findByUserIdAndStatus(userId, "ACTIVE"))
                    .thenReturn(List.of(new com.sports.entity.Goal()));
            when(goalRepository.findByUserIdAndStatus(userId, "COMPLETED"))
                    .thenReturn(List.of(new com.sports.entity.Goal(), new com.sports.entity.Goal()));

            StatsResponse response = statsService.getOverview(userId);

            assertThat(response.getTotalExercises()).isEqualTo(10);
            assertThat(response.getTotalDuration()).isEqualTo(300);
            assertThat(response.getTotalCalories()).isEqualTo(2500);
            assertThat(response.getActiveGoals()).isEqualTo(1);
            assertThat(response.getCompletedGoals()).isEqualTo(2);
        }

        @Test
        @DisplayName("Repository返回null时totalExercises兜底为0")
        void getOverview_nullCount() {
            when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
            when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
            when(goalRepository.findByUserIdAndStatus(eq(userId), any())).thenReturn(Collections.emptyList());

            StatsResponse response = statsService.getOverview(userId);

            assertThat(response.getTotalExercises()).isEqualTo(0);
            assertThat(response.getTotalDuration()).isEqualTo(0);
            assertThat(response.getTotalCalories()).isEqualTo(0);
        }

        @Test
        @DisplayName("无目标时activeGoals和completedGoals均为0")
        void getOverview_noGoals() {
            when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(5L);
            when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(100);
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(500);
            when(goalRepository.findByUserIdAndStatus(eq(userId), any())).thenReturn(Collections.emptyList());

            StatsResponse response = statsService.getOverview(userId);

            assertThat(response.getActiveGoals()).isEqualTo(0);
            assertThat(response.getCompletedGoals()).isEqualTo(0);
        }

        @Test
        @DisplayName("月区间从当月1号到月末")
        void getOverview_monthRange() {
            when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(1L);
            when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(0);
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(0);
            when(goalRepository.findByUserIdAndStatus(eq(userId), any())).thenReturn(Collections.emptyList());

            statsService.getOverview(userId);

            LocalDate now = LocalDate.now();
            verify(exerciseRepository).countByUserIdAndDateRange(eq(userId),
                    eq(now.withDayOfMonth(1)), eq(now.withDayOfMonth(now.lengthOfMonth())));
        }
    }

    @Nested
    @DisplayName("getWeeklyStats")
    class GetWeeklyStatsTests {

        @Test
        @DisplayName("正常获取周统计数据")
        void getWeeklyStats_success() {
            when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(3L);
            when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(180);
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(1200);

            Map<String, Object> stats = statsService.getWeeklyStats(userId);

            assertThat(stats.get("count")).isEqualTo(3);
            assertThat(stats.get("duration")).isEqualTo(180);
            assertThat(stats.get("calories")).isEqualTo(1200);
            assertThat(stats).containsKeys("startDate", "endDate");
        }

        @Test
        @DisplayName("Repository返回null时兜底为0")
        void getWeeklyStats_nullReturns() {
            when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
            when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);

            Map<String, Object> stats = statsService.getWeeklyStats(userId);

            assertThat(stats.get("count")).isEqualTo(0);
            assertThat(stats.get("duration")).isEqualTo(0);
            assertThat(stats.get("calories")).isEqualTo(0);
        }

        @Test
        @DisplayName("周区间从周一开始")
        void getWeeklyStats_startsOnMonday() {
            when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(0L);
            when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(0);
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(0);

            statsService.getWeeklyStats(userId);

            verify(exerciseRepository).countByUserIdAndDateRange(eq(userId),
                    argThat(date -> date.getDayOfWeek().getValue() == 1), any());
        }
    }

    @Nested
    @DisplayName("getMonthlyStats")
    class GetMonthlyStatsTests {

        @Test
        @DisplayName("正常获取月统计数据")
        void getMonthlyStats_success() {
            when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(8L);
            when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(400);
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(3000);

            Map<String, Object> stats = statsService.getMonthlyStats(userId);

            assertThat(stats.get("count")).isEqualTo(8);
            assertThat(stats.get("duration")).isEqualTo(400);
            assertThat(stats.get("calories")).isEqualTo(3000);
            assertThat(stats).containsKeys("month", "year");
        }

        @Test
        @DisplayName("Repository返回null时兜底为0")
        void getMonthlyStats_nullReturns() {
            when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
            when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(null);

            Map<String, Object> stats = statsService.getMonthlyStats(userId);

            assertThat(stats.get("count")).isEqualTo(0);
            assertThat(stats.get("duration")).isEqualTo(0);
            assertThat(stats.get("calories")).isEqualTo(0);
        }

        @Test
        @DisplayName("month和year字段为当前值")
        void getMonthlyStats_currentMonthYear() {
            when(exerciseRepository.countByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(0L);
            when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(0);
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(userId), any(), any())).thenReturn(0);

            Map<String, Object> stats = statsService.getMonthlyStats(userId);

            LocalDate now = LocalDate.now();
            assertThat(stats.get("month")).isEqualTo(now.getMonthValue());
            assertThat(stats.get("year")).isEqualTo(now.getYear());
        }
    }

    @Nested
    @DisplayName("getCaloriesTrend")
    class GetCaloriesTrendTests {

        @Test
        @DisplayName("正常获取卡路里趋势并补全缺失日期")
        void getCaloriesTrend_fillsMissingDates() {
            LocalDate today = LocalDate.now();
            Object[] row1 = new Object[]{today, 100L};
            Object[] row2 = new Object[]{today.minusDays(2), 50L};

            when(exerciseRepository.getCaloriesTrendByDateRange(eq(userId), any(), any()))
                    .thenReturn(List.<Object[]>of(row1, row2));

            List<Map<String, Object>> trend = statsService.getCaloriesTrend(userId, 5);

            assertThat(trend).hasSize(5);
            assertThat(trend.get(4).get("date")).isEqualTo(today.toString());
            assertThat(trend.get(4).get("calories")).isEqualTo(100);
            assertThat(trend.get(3).get("date")).isEqualTo(today.minusDays(1).toString());
            assertThat(trend.get(3).get("calories")).isEqualTo(0);
            assertThat(trend.get(2).get("date")).isEqualTo(today.minusDays(2).toString());
            assertThat(trend.get(2).get("calories")).isEqualTo(50);
        }

        @Test
        @DisplayName("无数据时所有日期calories为0")
        void getCaloriesTrend_noData() {
            when(exerciseRepository.getCaloriesTrendByDateRange(eq(userId), any(), any()))
                    .thenReturn(Collections.emptyList());

            List<Map<String, Object>> trend = statsService.getCaloriesTrend(userId, 3);

            assertThat(trend).hasSize(3);
            assertThat(trend).allMatch(item -> item.get("calories").equals(0));
        }

        @Test
        @DisplayName("days=1时只返回今天")
        void getCaloriesTrend_singleDay() {
            when(exerciseRepository.getCaloriesTrendByDateRange(eq(userId), any(), any()))
                    .thenReturn(Collections.emptyList());

            List<Map<String, Object>> trend = statsService.getCaloriesTrend(userId, 1);

            assertThat(trend).hasSize(1);
            assertThat(trend.get(0).get("date")).isEqualTo(LocalDate.now().toString());
        }

        @Test
        @DisplayName("日卡路里为null时兜底为0")
        void getCaloriesTrend_nullCalories() {
            LocalDate today = LocalDate.now();
            Object[] row = new Object[]{today, null};

            when(exerciseRepository.getCaloriesTrendByDateRange(eq(userId), any(), any()))
                    .thenReturn(List.<Object[]>of(row));

            List<Map<String, Object>> trend = statsService.getCaloriesTrend(userId, 1);

            assertThat(trend.get(0).get("calories")).isEqualTo(0);
        }

        @Test
        @DisplayName("日期序列按时间升序排列")
        void getCaloriesTrend_sortedAsc() {
            when(exerciseRepository.getCaloriesTrendByDateRange(eq(userId), any(), any()))
                    .thenReturn(Collections.emptyList());

            List<Map<String, Object>> trend = statsService.getCaloriesTrend(userId, 5);

            for (int i = 1; i < trend.size(); i++) {
                LocalDate prev = LocalDate.parse((String) trend.get(i - 1).get("date"));
                LocalDate curr = LocalDate.parse((String) trend.get(i).get("date"));
                assertThat(curr).isAfter(prev);
            }
        }
    }

    @Nested
    @DisplayName("getExerciseTypeDistribution")
    class GetExerciseTypeDistributionTests {

        @Test
        @DisplayName("正常获取运动类型分布")
        void getExerciseTypeDistribution_success() {
            Object[] row1 = new Object[]{"跑步", 10L};
            Object[] row2 = new Object[]{"游泳", 5L};

            when(exerciseRepository.countByExerciseType(userId)).thenReturn(List.<Object[]>of(row1, row2));

            List<Map<String, Object>> distribution = statsService.getExerciseTypeDistribution(userId);

            assertThat(distribution).hasSize(2);
            assertThat(distribution.get(0).get("type")).isEqualTo("跑步");
            assertThat(distribution.get(0).get("count")).isEqualTo(10);
            assertThat(distribution.get(1).get("type")).isEqualTo("游泳");
            assertThat(distribution.get(1).get("count")).isEqualTo(5);
        }

        @Test
        @DisplayName("无数据时返回空列表")
        void getExerciseTypeDistribution_noData() {
            when(exerciseRepository.countByExerciseType(userId)).thenReturn(Collections.emptyList());

            List<Map<String, Object>> distribution = statsService.getExerciseTypeDistribution(userId);

            assertThat(distribution).isEmpty();
        }
    }
}
