package com.sports.repository;

import com.sports.entity.Exercise;
import com.sports.entity.ExerciseType;
import com.sports.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ExerciseRepository 单元测试")
class ExerciseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExerciseRepository exerciseRepository;

    private User user;
    private ExerciseType runningType;
    private ExerciseType swimmingType;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("t***@***********");
        entityManager.persist(user);

        runningType = new ExerciseType();
        runningType.setName("跑步");
        runningType.setCaloriesPerMinute(10);
        entityManager.persist(runningType);

        swimmingType = new ExerciseType();
        swimmingType.setName("游泳");
        swimmingType.setCaloriesPerMinute(8);
        entityManager.persist(swimmingType);
    }

    @Test
    @DisplayName("sumCaloriesByUserIdAndDateRange - 正常聚合卡路里")
    void testSumCaloriesByUserIdAndDateRange() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 10);

        createExercise(user, runningType, LocalDate.of(2026, 5, 3), 30, 300);
        createExercise(user, runningType, LocalDate.of(2026, 5, 5), 60, 600);
        createExercise(user, runningType, LocalDate.of(2026, 5, 15), 20, 200);

        Integer result = exerciseRepository.sumCaloriesByUserIdAndDateRange(user.getId(), start, end);

        assertEquals(Integer.valueOf(900), result);
    }

    @Test
    @DisplayName("sumCaloriesByUserIdAndDateRange - 无数据时返回 null")
    void testSumCaloriesByUserIdAndDateRange_NoData() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        Integer result = exerciseRepository.sumCaloriesByUserIdAndDateRange(user.getId(), start, end);

        assertNull(result);
    }

    @Test
    @DisplayName("sumDurationByUserIdAndDateRange - 正常聚合时长")
    void testSumDurationByUserIdAndDateRange() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 10);

        createExercise(user, runningType, LocalDate.of(2026, 5, 3), 30, 300);
        createExercise(user, swimmingType, LocalDate.of(2026, 5, 5), 45, 360);

        Integer result = exerciseRepository.sumDurationByUserIdAndDateRange(user.getId(), start, end);

        assertEquals(Integer.valueOf(75), result);
    }

    @Test
    @DisplayName("sumDurationByUserIdAndDateRange - 无数据时返回 null")
    void testSumDurationByUserIdAndDateRange_NoData() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        Integer result = exerciseRepository.sumDurationByUserIdAndDateRange(user.getId(), start, end);

        assertNull(result);
    }

    @Test
    @DisplayName("countByUserIdAndDateRange - 统计范围内运动次数")
    void testCountByUserIdAndDateRange() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 10);

        createExercise(user, runningType, LocalDate.of(2026, 5, 3), 30, 300);
        createExercise(user, swimmingType, LocalDate.of(2026, 5, 5), 45, 360);
        createExercise(user, runningType, LocalDate.of(2026, 5, 20), 20, 200);

        Long result = exerciseRepository.countByUserIdAndDateRange(user.getId(), start, end);

        assertEquals(Long.valueOf(2), result);
    }

    @Test
    @DisplayName("countByUserIdAndDateRange - 无数据时返回 0")
    void testCountByUserIdAndDateRange_NoData() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        Long result = exerciseRepository.countByUserIdAndDateRange(user.getId(), start, end);

        assertEquals(Long.valueOf(0), result);
    }

    @Test
    @DisplayName("countByExerciseType - 按运动类型分组统计")
    void testCountByExerciseType() {
        createExercise(user, runningType, LocalDate.of(2026, 5, 3), 30, 300);
        createExercise(user, runningType, LocalDate.of(2026, 5, 5), 45, 450);
        createExercise(user, swimmingType, LocalDate.of(2026, 5, 4), 60, 480);

        List<Object[]> result = exerciseRepository.countByExerciseType(user.getId());

        assertEquals(2, result.size());
        for (Object[] row : result) {
            String type = (String) row[0];
            Long count = (Long) row[1];
            if (type.equals("跑步")) {
                assertEquals(Long.valueOf(2), count);
            } else if (type.equals("游泳")) {
                assertEquals(Long.valueOf(1), count);
            } else {
                fail("未知运动类型: " + type);
            }
        }
    }

    @Test
    @DisplayName("countByExerciseType - 无数据时返回空列表")
    void testCountByExerciseType_NoData() {
        List<Object[]> result = exerciseRepository.countByExerciseType(user.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getCaloriesTrendByDateRange - 按日期分组的卡路里趋势")
    void testGetCaloriesTrendByDateRange() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 5);

        createExercise(user, runningType, LocalDate.of(2026, 5, 2), 30, 300);
        createExercise(user, runningType, LocalDate.of(2026, 5, 2), 20, 200);
        createExercise(user, swimmingType, LocalDate.of(2026, 5, 4), 60, 480);

        List<Object[]> result = exerciseRepository.getCaloriesTrendByDateRange(user.getId(), start, end);

        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2026, 5, 2), result.get(0)[0]);
        assertEquals(Long.valueOf(500), result.get(0)[1]);
        assertEquals(LocalDate.of(2026, 5, 4), result.get(1)[0]);
        assertEquals(Long.valueOf(480), result.get(1)[1]);
    }

    @Test
    @DisplayName("getCaloriesTrendByDateRange - 无数据时返回空列表")
    void testGetCaloriesTrendByDateRange_NoData() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        List<Object[]> result = exerciseRepository.getCaloriesTrendByDateRange(user.getId(), start, end);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("边界日期 - 包含开始和结束日期")
    void testBoundaryDates() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 5);

        createExercise(user, runningType, LocalDate.of(2026, 5, 1), 30, 300);
        createExercise(user, runningType, LocalDate.of(2026, 5, 5), 20, 200);

        Integer calories = exerciseRepository.sumCaloriesByUserIdAndDateRange(user.getId(), start, end);
        Long count = exerciseRepository.countByUserIdAndDateRange(user.getId(), start, end);

        assertEquals(Integer.valueOf(500), calories);
        assertEquals(Long.valueOf(2), count);
    }

    private void createExercise(User user, ExerciseType type, LocalDate date, int duration, int calories) {
        Exercise exercise = new Exercise();
        exercise.setUser(user);
        exercise.setExerciseType(type);
        exercise.setDurationMinutes(duration);
        exercise.setCaloriesBurned(calories);
        exercise.setDistanceKm(BigDecimal.valueOf(5.0));
        exercise.setExerciseDate(date);
        entityManager.persist(exercise);
    }
}
