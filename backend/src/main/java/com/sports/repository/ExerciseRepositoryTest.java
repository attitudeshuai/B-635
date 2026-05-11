package com.sports.repository;

import com.sports.entity.Exercise;
import com.sports.entity.ExerciseType;
import com.sports.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ExerciseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExerciseRepository exerciseRepository;

    private User testUser;
    private ExerciseType runningType;
    private ExerciseType swimmingType;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);

        runningType = new ExerciseType();
        runningType.setName("Running");
        runningType.setCaloriesPerMinute(10);
        entityManager.persist(runningType);

        swimmingType = new ExerciseType();
        swimmingType.setName("Swimming");
        swimmingType.setCaloriesPerMinute(8);
        entityManager.persist(swimmingType);

        startDate = LocalDate.now().minusDays(7);
        endDate = LocalDate.now().plusDays(7);
    }

    @Test
    void sumCaloriesByUserIdAndDateRange_ShouldReturnCorrectSum() {
        createExercise(testUser, runningType, 30, 300, LocalDate.now());
        createExercise(testUser, runningType, 45, 450, LocalDate.now().minusDays(1));
        createExercise(testUser, swimmingType, 60, 480, LocalDate.now().minusDays(3));

        Integer result = exerciseRepository.sumCaloriesByUserIdAndDateRange(
                testUser.getId(), startDate, endDate);

        assertEquals(1230, result);
    }

    @Test
    void sumCaloriesByUserIdAndDateRange_WithNoExercises_ShouldReturnNull() {
        Integer result = exerciseRepository.sumCaloriesByUserIdAndDateRange(
                testUser.getId(), startDate, endDate);

        assertNull(result);
    }

    @Test
    void sumCaloriesByUserIdAndDateRange_OutsideDateRange_ShouldReturnNull() {
        createExercise(testUser, runningType, 30, 300, LocalDate.now().minusDays(14));

        Integer result = exerciseRepository.sumCaloriesByUserIdAndDateRange(
                testUser.getId(), startDate, endDate);

        assertNull(result);
    }

    @Test
    void sumDurationByUserIdAndDateRange_ShouldReturnCorrectSum() {
        createExercise(testUser, runningType, 30, 300, LocalDate.now());
        createExercise(testUser, runningType, 45, 450, LocalDate.now().minusDays(1));
        createExercise(testUser, swimmingType, 60, 480, LocalDate.now().minusDays(3));

        Integer result = exerciseRepository.sumDurationByUserIdAndDateRange(
                testUser.getId(), startDate, endDate);

        assertEquals(135, result);
    }

    @Test
    void sumDurationByUserIdAndDateRange_WithNoExercises_ShouldReturnNull() {
        Integer result = exerciseRepository.sumDurationByUserIdAndDateRange(
                testUser.getId(), startDate, endDate);

        assertNull(result);
    }

    @Test
    void countByUserIdAndDateRange_ShouldReturnCorrectCount() {
        createExercise(testUser, runningType, 30, 300, LocalDate.now());
        createExercise(testUser, runningType, 45, 450, LocalDate.now().minusDays(1));
        createExercise(testUser, swimmingType, 60, 480, LocalDate.now().minusDays(3));

        Long result = exerciseRepository.countByUserIdAndDateRange(
                testUser.getId(), startDate, endDate);

        assertEquals(3, result);
    }

    @Test
    void countByUserIdAndDateRange_WithNoExercises_ShouldReturnZero() {
        Long result = exerciseRepository.countByUserIdAndDateRange(
                testUser.getId(), startDate, endDate);

        assertEquals(0, result);
    }

    @Test
    void countByExerciseType_ShouldReturnCorrectDistribution() {
        createExercise(testUser, runningType, 30, 300, LocalDate.now());
        createExercise(testUser, runningType, 45, 450, LocalDate.now().minusDays(1));
        createExercise(testUser, swimmingType, 60, 480, LocalDate.now().minusDays(3));

        List<Object[]> result = exerciseRepository.countByExerciseType(testUser.getId());

        assertEquals(2, result.size());
        
        long runningCount = 0;
        long swimmingCount = 0;
        for (Object[] row : result) {
            String type = (String) row[0];
            Long count = (Long) row[1];
            if ("Running".equals(type)) {
                runningCount = count;
            } else if ("Swimming".equals(type)) {
                swimmingCount = count;
            }
        }
        assertEquals(2, runningCount);
        assertEquals(1, swimmingCount);
    }

    @Test
    void countByExerciseType_WithNoExercises_ShouldReturnEmptyList() {
        List<Object[]> result = exerciseRepository.countByExerciseType(testUser.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void getCaloriesTrendByDateRange_ShouldReturnDailySums() {
        LocalDate day1 = LocalDate.now();
        LocalDate day2 = LocalDate.now().minusDays(1);
        LocalDate day3 = LocalDate.now().minusDays(2);

        createExercise(testUser, runningType, 30, 300, day1);
        createExercise(testUser, runningType, 45, 450, day1);
        createExercise(testUser, swimmingType, 60, 480, day2);

        List<Object[]> result = exerciseRepository.getCaloriesTrendByDateRange(
                testUser.getId(), day3, day1);

        assertEquals(2, result.size());
    }

    @Test
    void getCaloriesTrendByDateRange_WithNoExercises_ShouldReturnEmptyList() {
        List<Object[]> result = exerciseRepository.getCaloriesTrendByDateRange(
                testUser.getId(), startDate, endDate);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserIdAndExerciseDateBetween_ShouldReturnCorrectExercises() {
        Exercise ex1 = createExercise(testUser, runningType, 30, 300, LocalDate.now());
        Exercise ex2 = createExercise(testUser, runningType, 45, 450, LocalDate.now().minusDays(1));
        createExercise(testUser, swimmingType, 60, 480, LocalDate.now().minusDays(14));

        List<Exercise> result = exerciseRepository.findByUserIdAndExerciseDateBetween(
                testUser.getId(), startDate, endDate);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(ex1.getId())));
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(ex2.getId())));
    }

    @Test
    void sumCaloriesByUserIdAndDateRange_ShouldOnlySumForSpecifiedUser() {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword("password");
        otherUser.setEmail("other@example.com");
        entityManager.persist(otherUser);

        createExercise(testUser, runningType, 30, 300, LocalDate.now());
        createExercise(otherUser, runningType, 60, 600, LocalDate.now());

        Integer result = exerciseRepository.sumCaloriesByUserIdAndDateRange(
                testUser.getId(), startDate, endDate);

        assertEquals(300, result);
    }

    private Exercise createExercise(User user, ExerciseType type, int duration, int calories, LocalDate date) {
        Exercise exercise = new Exercise();
        exercise.setUser(user);
        exercise.setExerciseType(type);
        exercise.setDurationMinutes(duration);
        exercise.setCaloriesBurned(calories);
        exercise.setExerciseDate(date);
        return entityManager.persist(exercise);
    }
}
