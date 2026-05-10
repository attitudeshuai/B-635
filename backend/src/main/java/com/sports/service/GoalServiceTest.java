package com.sports.service;

import com.sports.dto.GoalRequest;
import com.sports.dto.GoalResponse;
import com.sports.entity.Goal;
import com.sports.entity.User;
import com.sports.exception.BusinessException;
import com.sports.repository.ExerciseRepository;
import com.sports.repository.GoalRepository;
import com.sports.repository.UserRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private GoalService goalService;

    private User defaultUser;
    private Goal defaultGoal;
    private GoalRequest defaultRequest;

    @BeforeEach
    void setUp() {
        defaultUser = new User();
        defaultUser.setId(1L);
        defaultUser.setUsername("testuser");
        defaultUser.setEmail("test@example.com");

        defaultGoal = new Goal();
        defaultGoal.setId(10L);
        defaultGoal.setUser(defaultUser);
        defaultGoal.setGoalType("CALORIES");
        defaultGoal.setTargetValue(1000);
        defaultGoal.setCurrentValue(0);
        defaultGoal.setStartDate(LocalDate.of(2026, 1, 1));
        defaultGoal.setEndDate(LocalDate.of(2026, 12, 31));
        defaultGoal.setStatus("ACTIVE");
        defaultGoal.setTitle("年度卡路里目标");

        defaultRequest = new GoalRequest();
        defaultRequest.setGoalType("CALORIES");
        defaultRequest.setTargetValue(1000);
        defaultRequest.setStartDate(LocalDate.of(2026, 1, 1));
        defaultRequest.setEndDate(LocalDate.of(2026, 12, 31));
        defaultRequest.setTitle("年度卡路里目标");
    }

    @Nested
    @DisplayName("createGoal")
    class CreateGoalTests {

        @Test
        @DisplayName("正常创建目标并触发进度计算")
        void createGoal_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(defaultUser));
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> {
                Goal g = invocation.getArgument(0);
                g.setId(10L);
                return g;
            });
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(500);

            GoalResponse response = goalService.createGoal(1L, defaultRequest);

            assertThat(response).isNotNull();
            assertThat(response.getGoalType()).isEqualTo("CALORIES");
            assertThat(response.getTargetValue()).isEqualTo(1000);
            assertThat(response.getCurrentValue()).isEqualTo(500);
            assertThat(response.getTitle()).isEqualTo("年度卡路里目标");
            verify(goalRepository, times(2)).save(any(Goal.class));
        }

        @Test
        @DisplayName("用户不存在时抛出BusinessException")
        void createGoal_userNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalService.createGoal(999L, defaultRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("用户不存在");
        }

        @Test
        @DisplayName("结束日期早于开始日期时抛出BusinessException")
        void createGoal_endDateBeforeStartDate() {
            defaultRequest.setStartDate(LocalDate.of(2026, 12, 31));
            defaultRequest.setEndDate(LocalDate.of(2026, 1, 1));
            when(userRepository.findById(1L)).thenReturn(Optional.of(defaultUser));

            assertThatThrownBy(() -> goalService.createGoal(1L, defaultRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("结束日期不能早于开始日期");
        }

        @Test
        @DisplayName("创建目标初始currentValue为0且status为ACTIVE")
        void createGoal_initialState() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(defaultUser));
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> {
                Goal g = invocation.getArgument(0);
                g.setId(10L);
                return g;
            });
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(0);

            goalService.createGoal(1L, defaultRequest);

            verify(goalRepository, atLeastOnce()).save(argThat(goal ->
                    goal.getStatus().equals("ACTIVE")
            ));
        }
    }

    @Nested
    @DisplayName("updateGoal")
    class UpdateGoalTests {

        @Test
        @DisplayName("正常更新目标并重算进度")
        void updateGoal_success() {
            when(goalRepository.findById(10L)).thenReturn(Optional.of(defaultGoal));
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(800);

            GoalRequest updateReq = new GoalRequest();
            updateReq.setGoalType("CALORIES");
            updateReq.setTargetValue(2000);
            updateReq.setStartDate(LocalDate.of(2026, 1, 1));
            updateReq.setEndDate(LocalDate.of(2026, 12, 31));
            updateReq.setTitle("更新后的目标");

            GoalResponse response = goalService.updateGoal(1L, 10L, updateReq);

            assertThat(response.getTargetValue()).isEqualTo(2000);
            assertThat(response.getCurrentValue()).isEqualTo(800);
            assertThat(response.getTitle()).isEqualTo("更新后的目标");
        }

        @Test
        @DisplayName("目标不存在时抛出BusinessException")
        void updateGoal_goalNotFound() {
            when(goalRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalService.updateGoal(1L, 999L, defaultRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("目标不存在");
        }

        @Test
        @DisplayName("非本人目标无权操作")
        void updateGoal_notOwner() {
            User otherUser = new User();
            otherUser.setId(2L);
            defaultGoal.setUser(otherUser);

            when(goalRepository.findById(10L)).thenReturn(Optional.of(defaultGoal));

            assertThatThrownBy(() -> goalService.updateGoal(1L, 10L, defaultRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("无权操作此目标");
        }
    }

    @Nested
    @DisplayName("deleteGoal")
    class DeleteGoalTests {

        @Test
        @DisplayName("正常删除目标")
        void deleteGoal_success() {
            when(goalRepository.findById(10L)).thenReturn(Optional.of(defaultGoal));

            goalService.deleteGoal(1L, 10L);

            verify(goalRepository).delete(defaultGoal);
        }

        @Test
        @DisplayName("目标不存在时抛出BusinessException")
        void deleteGoal_goalNotFound() {
            when(goalRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalService.deleteGoal(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("目标不存在");
        }

        @Test
        @DisplayName("非本人目标无权删除")
        void deleteGoal_notOwner() {
            User otherUser = new User();
            otherUser.setId(2L);
            defaultGoal.setUser(otherUser);

            when(goalRepository.findById(10L)).thenReturn(Optional.of(defaultGoal));

            assertThatThrownBy(() -> goalService.deleteGoal(1L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("无权操作此目标");
        }
    }

    @Nested
    @DisplayName("getGoalsByUserId")
    class GetGoalsTests {

        @Test
        @DisplayName("返回列表且每条都经过进度更新")
        void getGoals_returnsUpdatedProgress() {
            defaultGoal.setCurrentValue(0);
            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(300);

            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getCurrentValue()).isEqualTo(300);
        }

        @Test
        @DisplayName("无目标时返回空列表")
        void getGoals_emptyList() {
            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());

            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateGoalProgress 间接测试")
    class UpdateGoalProgressTests {

        private void prepareGoalForProgressUpdate() {
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        }

        @Test
        @DisplayName("CALORIES类型调用sumCaloriesByUserIdAndDateRange")
        void progress_CALORIES() {
            defaultGoal.setGoalType("CALORIES");
            prepareGoalForProgressUpdate();
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(750);

            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));
            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses.get(0).getCurrentValue()).isEqualTo(750);
            verify(exerciseRepository).sumCaloriesByUserIdAndDateRange(eq(1L), any(), any());
        }

        @Test
        @DisplayName("DURATION类型调用sumDurationByUserIdAndDateRange")
        void progress_DURATION() {
            defaultGoal.setGoalType("DURATION");
            prepareGoalForProgressUpdate();
            when(exerciseRepository.sumDurationByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(120);

            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));
            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses.get(0).getCurrentValue()).isEqualTo(120);
            verify(exerciseRepository).sumDurationByUserIdAndDateRange(eq(1L), any(), any());
        }

        @Test
        @DisplayName("COUNT类型调用countByUserIdAndDateRange")
        void progress_COUNT() {
            defaultGoal.setGoalType("COUNT");
            prepareGoalForProgressUpdate();
            when(exerciseRepository.countByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(5L);

            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));
            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses.get(0).getCurrentValue()).isEqualTo(5);
            verify(exerciseRepository).countByUserIdAndDateRange(eq(1L), any(), any());
        }

        @Test
        @DisplayName("Repository返回null时currentValue兜底为0")
        void progress_nullReturns() {
            defaultGoal.setGoalType("CALORIES");
            prepareGoalForProgressUpdate();
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(null);

            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));
            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses.get(0).getCurrentValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("count返回null时currentValue兜底为0")
        void progress_countNull() {
            defaultGoal.setGoalType("COUNT");
            prepareGoalForProgressUpdate();
            when(exerciseRepository.countByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(null);

            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));
            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses.get(0).getCurrentValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("currentValue>=targetValue时status变为COMPLETED")
        void progress_completedStatus() {
            defaultGoal.setGoalType("CALORIES");
            defaultGoal.setTargetValue(1000);
            prepareGoalForProgressUpdate();
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(1000);

            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));
            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses.get(0).getStatus()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("currentValue超过targetValue时status也为COMPLETED")
        void progress_exceedsTarget() {
            defaultGoal.setGoalType("CALORIES");
            defaultGoal.setTargetValue(1000);
            prepareGoalForProgressUpdate();
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(1500);

            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));
            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses.get(0).getStatus()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("当前日期超过endDate且未完成时status变为FAILED")
        void progress_failedStatus() {
            defaultGoal.setGoalType("CALORIES");
            defaultGoal.setTargetValue(1000);
            defaultGoal.setEndDate(LocalDate.now().minusDays(1));
            prepareGoalForProgressUpdate();
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(500);

            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));
            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses.get(0).getStatus()).isEqualTo("FAILED");
        }

        @Test
        @DisplayName("endDate就是今天且未达标时status仍为ACTIVE")
        void progress_endDateToday_stillActive() {
            defaultGoal.setGoalType("CALORIES");
            defaultGoal.setTargetValue(1000);
            defaultGoal.setEndDate(LocalDate.now());
            prepareGoalForProgressUpdate();
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(500);

            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));
            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses.get(0).getStatus()).isEqualTo("ACTIVE");
        }
    }

    @Nested
    @DisplayName("toResponse 字段映射验证")
    class ToResponseTests {

        @Test
        @DisplayName("Goal到GoalResponse所有字段完整映射")
        void toResponse_allFieldsMapped() {
            defaultGoal.setCurrentValue(500);
            defaultGoal.setStatus("ACTIVE");
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(500);
            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));

            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);
            GoalResponse resp = responses.get(0);

            assertThat(resp.getId()).isEqualTo(10L);
            assertThat(resp.getGoalType()).isEqualTo("CALORIES");
            assertThat(resp.getTargetValue()).isEqualTo(1000);
            assertThat(resp.getCurrentValue()).isEqualTo(500);
            assertThat(resp.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(resp.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
            assertThat(resp.getStatus()).isEqualTo("ACTIVE");
            assertThat(resp.getTitle()).isEqualTo("年度卡路里目标");
        }

        @Test
        @DisplayName("progress字段由Goal.getProgress()计算-50%场景")
        void toResponse_progressHalf() {
            defaultGoal.setTargetValue(1000);
            defaultGoal.setCurrentValue(500);
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(500);
            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));

            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses.get(0).getProgress()).isEqualTo(50);
        }

        @Test
        @DisplayName("progress上限为100-超额场景")
        void toResponse_progressCappedAt100() {
            defaultGoal.setTargetValue(100);
            defaultGoal.setCurrentValue(200);
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(200);
            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));

            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses.get(0).getProgress()).isEqualTo(100);
        }

        @Test
        @DisplayName("targetValue为0时progress为0")
        void toResponse_zeroTarget() {
            defaultGoal.setTargetValue(0);
            defaultGoal.setCurrentValue(500);
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(500);
            when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(defaultGoal));

            List<GoalResponse> responses = goalService.getGoalsByUserId(1L);

            assertThat(responses.get(0).getProgress()).isEqualTo(0);
        }
    }
}
