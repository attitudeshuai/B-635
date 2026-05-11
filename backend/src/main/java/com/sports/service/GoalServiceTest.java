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
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private GoalService goalService;

    private User testUser;
    private Goal testGoal;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        startDate = LocalDate.now().minusDays(7);
        endDate = LocalDate.now().plusDays(7);

        testGoal = new Goal();
        testGoal.setId(1L);
        testGoal.setUser(testUser);
        testGoal.setGoalType("CALORIES");
        testGoal.setTargetValue(1000);
        testGoal.setCurrentValue(0);
        testGoal.setStartDate(startDate);
        testGoal.setEndDate(endDate);
        testGoal.setTitle("Test Goal");
        testGoal.setStatus("ACTIVE");
    }

    @Test
    void getGoalsByUserId_ShouldReturnGoalsWithUpdatedProgress() {
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(testGoal));
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(eq(1L), any(), any())).thenReturn(500);
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);

        assertEquals(1, result.size());
        GoalResponse response = result.get(0);
        assertEquals(500, response.getCurrentValue());
        assertEquals(50, response.getProgress());
        verify(exerciseRepository).sumCaloriesByUserIdAndDateRange(eq(1L), any(), any());
    }

    @Test
    void updateGoalProgress_CaloriesType_ShouldCalculateCorrectly() {
        testGoal.setGoalType("CALORIES");
        testGoal.setTargetValue(2000);

        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(1L, startDate, endDate)).thenReturn(1500);
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(testGoal));

        goalService.getGoalsByUserId(1L);

        verify(exerciseRepository).sumCaloriesByUserIdAndDateRange(1L, startDate, endDate);
    }

    @Test
    void updateGoalProgress_DurationType_ShouldCalculateCorrectly() {
        testGoal.setGoalType("DURATION");
        testGoal.setTargetValue(300);

        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(testGoal));
        when(exerciseRepository.sumDurationByUserIdAndDateRange(1L, startDate, endDate)).thenReturn(150);
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);

        assertEquals(150, result.get(0).getCurrentValue());
        assertEquals(50, result.get(0).getProgress());
        verify(exerciseRepository).sumDurationByUserIdAndDateRange(1L, startDate, endDate);
    }

    @Test
    void updateGoalProgress_CountType_ShouldCalculateCorrectly() {
        testGoal.setGoalType("COUNT");
        testGoal.setTargetValue(10);

        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(testGoal));
        when(exerciseRepository.countByUserIdAndDateRange(1L, startDate, endDate)).thenReturn(7L);
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);

        assertEquals(7, result.get(0).getCurrentValue());
        assertEquals(70, result.get(0).getProgress());
        verify(exerciseRepository).countByUserIdAndDateRange(1L, startDate, endDate);
    }

    @Test
    void updateGoalProgress_WhenCurrentValueMeetsTarget_ShouldSetStatusCompleted() {
        testGoal.setGoalType("CALORIES");
        testGoal.setTargetValue(1000);

        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(testGoal));
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(1L, startDate, endDate)).thenReturn(1000);
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);

        assertEquals("COMPLETED", result.get(0).getStatus());
        assertEquals(100, result.get(0).getProgress());
    }

    @Test
    void updateGoalProgress_WhenCurrentValueExceedsTarget_ShouldCapProgressAt100() {
        testGoal.setGoalType("CALORIES");
        testGoal.setTargetValue(1000);

        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(testGoal));
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(1L, startDate, endDate)).thenReturn(1500);
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);

        assertEquals(100, result.get(0).getProgress());
        assertEquals("COMPLETED", result.get(0).getStatus());
    }

    @Test
    void updateGoalProgress_WhenEndDatePassedAndNotCompleted_ShouldSetStatusFailed() {
        testGoal.setGoalType("CALORIES");
        testGoal.setTargetValue(1000);
        testGoal.setStartDate(LocalDate.now().minusDays(14));
        testGoal.setEndDate(LocalDate.now().minusDays(1));

        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(testGoal));
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(500);
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);

        assertEquals("FAILED", result.get(0).getStatus());
    }

    @Test
    void createGoal_ShouldInitializeCorrectly() {
        GoalRequest request = new GoalRequest();
        request.setGoalType("CALORIES");
        request.setTargetValue(1000);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setTitle("New Goal");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> {
            Goal saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(0);

        GoalResponse result = goalService.createGoal(1L, request);

        assertNotNull(result);
        assertEquals("CALORIES", result.getGoalType());
        assertEquals(1000, result.getTargetValue());
        assertEquals(0, result.getCurrentValue());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(0, result.getProgress());
    }

    @Test
    void createGoal_WhenEndDateBeforeStartDate_ShouldThrowException() {
        GoalRequest request = new GoalRequest();
        request.setGoalType("CALORIES");
        request.setTargetValue(1000);
        request.setStartDate(endDate);
        request.setEndDate(startDate);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(BusinessException.class, () -> goalService.createGoal(1L, request));
    }

    @Test
    void createGoal_WhenUserNotFound_ShouldThrowException() {
        GoalRequest request = new GoalRequest();
        request.setGoalType("CALORIES");
        request.setTargetValue(1000);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> goalService.createGoal(1L, request));
    }

    @Test
    void toResponse_ShouldMapAllFieldsCorrectly() {
        testGoal.setCurrentValue(500);

        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(testGoal));
        when(exerciseRepository.sumCaloriesByUserIdAndDateRange(anyLong(), any(), any())).thenReturn(500);
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<GoalResponse> result = goalService.getGoalsByUserId(1L);
        GoalResponse response = result.get(0);

        assertEquals(testGoal.getId(), response.getId());
        assertEquals(testGoal.getGoalType(), response.getGoalType());
        assertEquals(testGoal.getTargetValue(), response.getTargetValue());
        assertEquals(testGoal.getCurrentValue(), response.getCurrentValue());
        assertEquals(testGoal.getStartDate(), response.getStartDate());
        assertEquals(testGoal.getEndDate(), response.getEndDate());
        assertEquals(testGoal.getStatus(), response.getStatus());
        assertEquals(testGoal.getTitle(), response.getTitle());
        assertNotNull(response.getProgress());
    }

    @Test
    void getProgress_WhenTargetValueIsZero_ShouldReturnZero() {
        testGoal.setTargetValue(0);
        testGoal.setCurrentValue(500);

        assertEquals(0, testGoal.getProgress());
    }

    @Test
    void getProgress_WhenTargetValueIsNull_ShouldReturnZero() {
        testGoal.setTargetValue(null);
        testGoal.setCurrentValue(500);

        assertEquals(0, testGoal.getProgress());
    }

    @Test
    void deleteGoal_WhenUserNotOwner_ShouldThrowException() {
        User otherUser = new User();
        otherUser.setId(2L);
        testGoal.setUser(otherUser);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));

        assertThrows(BusinessException.class, () -> goalService.deleteGoal(1L, 1L));
    }

    @Test
    void updateGoal_WhenGoalNotFound_ShouldThrowException() {
        GoalRequest request = new GoalRequest();
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> goalService.updateGoal(1L, 999L, request));
    }
}
