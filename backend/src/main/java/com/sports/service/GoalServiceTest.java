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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalService 单元测试")
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private GoalService goalService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    @Test
    @DisplayName("getGoalsByUserId - CALORIES 类型目标进度计算")
    void testGetGoalsByUserId_CaloriesType() {
        Goal goal = createGoal("CALORIES", 1000, 0);
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(goal));
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(500);

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);

        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(500), result.get(0).getCurrentValue());
        verify(exerciseRepository).sumCaloriesByUserIdAndDateRange(eq(1L), any(), any());
        verify(goalRepository).save(goal);
    }

    @Test
    @DisplayName("getGoalsByUserId - DURATION 类型目标进度计算")
    void testGetGoalsByUserId_DurationType() {
        Goal goal = createGoal("DURATION", 300, 0);
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(goal));
        when(exerciseRepository.sumDurationByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(150);

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);

        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(150), result.get(0).getCurrentValue());
        verify(exerciseRepository).sumDurationByUserIdAndDateRange(eq(1L), any(), any());
    }

    @Test
    @DisplayName("getGoalsByUserId - COUNT 类型目标进度计算")
    void testGetGoalsByUserId_CountType() {
        Goal goal = createGoal("COUNT", 10, 0);
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(goal));
        when(exerciseRepository.countByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(5L);

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);

        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(5), result.get(0).getCurrentValue());
        verify(exerciseRepository).countByUserIdAndDateRange(eq(1L), any(), any());
    }

    @Test
    @DisplayName("getGoalsByUserId - 无数据时 currentValue 为 0")
    void testGetGoalsByUserId_NoData() {
        Goal goal = createGoal("CALORIES", 1000, 0);
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(goal));
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(null);

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);

        assertEquals(Integer.valueOf(0), result.get(0).getCurrentValue());
    }

    @Test
    @DisplayName("状态更新 - 达到目标值自动更新为 COMPLETED")
    void testStatusUpdate_Completed() {
        Goal goal = createGoal("CALORIES", 1000, 0);
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(goal));
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(1200);

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);

        assertEquals("COMPLETED", result.get(0).getStatus());
        assertEquals(100, result.get(0).getProgress());
    }

    @Test
    @DisplayName("状态更新 - 超过结束日期未达成更新为 FAILED")
    void testStatusUpdate_Failed() {
        Goal goal = createGoal("CALORIES", 1000, 0);
        goal.setEndDate(LocalDate.now().minusDays(1));
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(goal));
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(500);

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);

        assertEquals("FAILED", result.get(0).getStatus());
    }

    @Test
    @DisplayName("状态更新 - 进行中保持 ACTIVE")
    void testStatusUpdate_Active() {
        Goal goal = createGoal("CALORIES", 1000, 0);
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(goal));
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(300);

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);

        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    @Test
    @DisplayName("createGoal - 正常创建目标")
    void testCreateGoal_Success() {
        GoalRequest request = new GoalRequest();
        request.setGoalType("CALORIES");
        request.setTargetValue(1000);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setTitle("测试目标");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(100);

        GoalResponse result = goalService.createGoal(1L, request);

        assertNotNull(result);
        assertEquals("CALORIES", result.getGoalType());
        assertEquals(Integer.valueOf(1000), result.getTargetValue());
        assertEquals("测试目标", result.getTitle());
        verify(goalRepository, times(2)).save(any(Goal.class));
    }

    @Test
    @DisplayName("createGoal - 用户不存在抛出异常")
    void testCreateGoal_UserNotFound() {
        GoalRequest request = new GoalRequest();
        request.setGoalType("CALORIES");
        request.setTargetValue(1000);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> goalService.createGoal(1L, request));
    }

    @Test
    @DisplayName("createGoal - 结束日期早于开始日期抛出异常")
    void testCreateGoal_InvalidDateRange() {
        GoalRequest request = new GoalRequest();
        request.setGoalType("CALORIES");
        request.setTargetValue(1000);
        request.setStartDate(LocalDate.now().plusDays(30));
        request.setEndDate(LocalDate.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> goalService.createGoal(1L, request));
    }

    @Test
    @DisplayName("updateGoal - 正常更新目标")
    void testUpdateGoal_Success() {
        Goal goal = createGoal("CALORIES", 1000, 500);
        GoalRequest request = new GoalRequest();
        request.setGoalType("DURATION");
        request.setTargetValue(500);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(60));
        request.setTitle("更新后的目标");

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(exerciseRepository.sumDurationByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(200);

        GoalResponse result = goalService.updateGoal(1L, 1L, request);

        assertEquals("DURATION", result.getGoalType());
        assertEquals(Integer.valueOf(500), result.getTargetValue());
        assertEquals(Integer.valueOf(200), result.getCurrentValue());
        assertEquals("更新后的目标", result.getTitle());
    }

    @Test
    @DisplayName("updateGoal - 目标不存在抛出异常")
    void testUpdateGoal_GoalNotFound() {
        GoalRequest request = new GoalRequest();
        request.setGoalType("CALORIES");
        request.setTargetValue(1000);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));

        when(goalRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> goalService.updateGoal(1L, 1L, request));
    }

    @Test
    @DisplayName("updateGoal - 无权限操作抛出异常")
    void testUpdateGoal_NoPermission() {
        User otherUser = new User();
        otherUser.setId(2L);
        Goal goal = createGoal("CALORIES", 1000, 500);
        goal.setUser(otherUser);

        GoalRequest request = new GoalRequest();
        request.setGoalType("CALORIES");
        request.setTargetValue(1000);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(BusinessException.class, () -> goalService.updateGoal(1L, 1L, request));
    }

    @Test
    @DisplayName("deleteGoal - 正常删除目标")
    void testDeleteGoal_Success() {
        Goal goal = createGoal("CALORIES", 1000, 500);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        doNothing().when(goalRepository).delete(goal);

        assertDoesNotThrow(() -> goalService.deleteGoal(1L, 1L));
        verify(goalRepository).delete(goal);
    }

    @Test
    @DisplayName("deleteGoal - 目标不存在抛出异常")
    void testDeleteGoal_GoalNotFound() {
        when(goalRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> goalService.deleteGoal(1L, 1L));
    }

    @Test
    @DisplayName("deleteGoal - 无权限操作抛出异常")
    void testDeleteGoal_NoPermission() {
        User otherUser = new User();
        otherUser.setId(2L);
        Goal goal = createGoal("CALORIES", 1000, 500);
        goal.setUser(otherUser);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(BusinessException.class, () -> goalService.deleteGoal(1L, 1L));
    }

    @Test
    @DisplayName("DTO 字段契约验证 - GoalResponse 必须包含所有前端需要的字段")
    void testDtoFieldContract() {
        Goal goal = createGoal("CALORIES", 1000, 500);
        goal.setId(1L);
        goal.setTitle("测试目标");
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(goal));
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(500);

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);
        GoalResponse response = result.get(0);

        assertNotNull(response.getId());
        assertNotNull(response.getGoalType());
        assertNotNull(response.getTargetValue());
        assertNotNull(response.getCurrentValue());
        assertNotNull(response.getStartDate());
        assertNotNull(response.getEndDate());
        assertNotNull(response.getStatus());
        assertNotNull(response.getTitle());
        assertNotNull(response.getProgress());
    }

    private Goal createGoal(String type, int target, int current) {
        Goal goal = new Goal();
        goal.setId(1L);
        goal.setUser(user);
        goal.setGoalType(type);
        goal.setTargetValue(target);
        goal.setCurrentValue(current);
        goal.setStartDate(LocalDate.now().minusDays(10));
        goal.setEndDate(LocalDate.now().plusDays(20));
        goal.setStatus("ACTIVE");
        goal.setTitle("测试目标");
        return goal;
    }
}
