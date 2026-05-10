package com.sports.repository;

import com.sports.entity.Exercise;
import com.sports.entity.ExerciseType;
import com.sports.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ActiveProfiles("test")
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
        user.setUsername("testuser_repo");
        user.setPassword("password");
        user.setEmail("repo@test.com");
        entityManager.persist(user);

        runningType = new ExerciseType();
        runningType.setName("跑步");
        runningType.setCaloriesPerMinute(10);
        entityManager.persist(runningType);

        swimmingType = new ExerciseType();
        swimmingType.setName("游泳");
        swimmingType.setCaloriesPerMinute(8);
        entityManager.persist(swimmingType);

        entityManager.flush();
    }

    private Exercise createExercise(LocalDate date, int duration, int calories, ExerciseType type) {
        Exercise ex = new Exercise();
        ex.setUser(user);
        ex.setExerciseType(type);
        ex.setDurationMinutes(duration);
        ex.setCaloriesBurned(calories);
        ex.setExerciseDate(date);
        entityManager.persist(ex);
        return ex;
    }

    @Nested
    @DisplayName("sumCaloriesByUserIdAndDateRange")
    class SumCaloriesTests {

        @Test
        @DisplayName("有匹配记录时返回卡路里合计")
        void sumCalories_withRecords() {
            createExercise(LocalDate.of(2026, 3, 10), 30, 300, runningType);
            createExercise(LocalDate.of(2026, 3, 15), 45, 360, swimmingType);
            entityManager.flush();

            Integer result = exerciseRepository.sumCaloriesByUserIdAndDateRange(
                    user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

            assertThat(result).isEqualTo(660);
        }

        @Test
        @DisplayName("无匹配记录时返回null")
        void sumCalories_noRecords() {
            Integer result = exerciseRepository.sumCaloriesByUserIdAndDateRange(
                    user.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("日期边界-包含起止日期")
        void sumCalories_boundaryInclusive() {
            createExercise(LocalDate.of(2026, 3, 1), 30, 300, runningType);
            createExercise(LocalDate.of(2026, 3, 31), 30, 300, runningType);
            createExercise(LocalDate.of(2026, 2, 28), 30, 300, runningType);
            createExercise(LocalDate.of(2026, 4, 1), 30, 300, runningType);
            entityManager.flush();

            Integer result = exerciseRepository.sumCaloriesByUserIdAndDateRange(
                    user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

            assertThat(result).isEqualTo(600);
        }
    }

    @Nested
    @DisplayName("sumDurationByUserIdAndDateRange")
    class SumDurationTests {

        @Test
        @DisplayName("有匹配记录时返回时长合计")
        void sumDuration_withRecords() {
            createExercise(LocalDate.of(2026, 3, 10), 30, 300, runningType);
            createExercise(LocalDate.of(2026, 3, 15), 45, 360, swimmingType);
            entityManager.flush();

            Integer result = exerciseRepository.sumDurationByUserIdAndDateRange(
                    user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

            assertThat(result).isEqualTo(75);
        }

        @Test
        @DisplayName("无匹配记录时返回null")
        void sumDuration_noRecords() {
            Integer result = exerciseRepository.sumDurationByUserIdAndDateRange(
                    user.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("日期边界-包含起止日期")
        void sumDuration_boundaryInclusive() {
            createExercise(LocalDate.of(2026, 3, 1), 20, 200, runningType);
            createExercise(LocalDate.of(2026, 3, 31), 25, 250, runningType);
            createExercise(LocalDate.of(2026, 2, 28), 30, 300, runningType);
            entityManager.flush();

            Integer result = exerciseRepository.sumDurationByUserIdAndDateRange(
                    user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

            assertThat(result).isEqualTo(45);
        }
    }

    @Nested
    @DisplayName("countByUserIdAndDateRange")
    class CountTests {

        @Test
        @DisplayName("有记录时返回正确计数")
        void count_withRecords() {
            createExercise(LocalDate.of(2026, 3, 10), 30, 300, runningType);
            createExercise(LocalDate.of(2026, 3, 15), 45, 360, swimmingType);
            createExercise(LocalDate.of(2026, 3, 20), 60, 600, runningType);
            entityManager.flush();

            Long result = exerciseRepository.countByUserIdAndDateRange(
                    user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

            assertThat(result).isEqualTo(3L);
        }

        @Test
        @DisplayName("无记录时返回0")
        void count_noRecords() {
            Long result = exerciseRepository.countByUserIdAndDateRange(
                    user.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("不同用户的记录不互相干扰")
        void count_differentUsers() {
            User otherUser = new User();
            otherUser.setUsername("otheruser");
            otherUser.setPassword("password");
            otherUser.setEmail("other@test.com");
            entityManager.persist(otherUser);

            Exercise otherEx = new Exercise();
            otherEx.setUser(otherUser);
            otherEx.setExerciseType(runningType);
            otherEx.setDurationMinutes(30);
            otherEx.setCaloriesBurned(300);
            otherEx.setExerciseDate(LocalDate.of(2026, 3, 10));
            entityManager.persist(otherEx);

            createExercise(LocalDate.of(2026, 3, 10), 30, 300, runningType);
            entityManager.flush();

            Long result = exerciseRepository.countByUserIdAndDateRange(
                    user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

            assertThat(result).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("countByExerciseType")
    class CountByExerciseTypeTests {

        @Test
        @DisplayName("按运动类型分组计数")
        void countByType_grouped() {
            createExercise(LocalDate.of(2026, 3, 10), 30, 300, runningType);
            createExercise(LocalDate.of(2026, 3, 11), 30, 300, runningType);
            createExercise(LocalDate.of(2026, 3, 12), 45, 360, swimmingType);
            entityManager.flush();

            List<Object[]> result = exerciseRepository.countByExerciseType(user.getId());

            assertThat(result).hasSize(2);
            assertThat(result).anyMatch(row -> row[0].equals("跑步") && ((Long) row[1]) == 2L);
            assertThat(result).anyMatch(row -> row[0].equals("游泳") && ((Long) row[1]) == 1L);
        }

        @Test
        @DisplayName("无记录时返回空列表")
        void countByType_noRecords() {
            List<Object[]> result = exerciseRepository.countByExerciseType(user.getId());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCaloriesTrendByDateRange")
    class CaloriesTrendTests {

        @Test
        @DisplayName("按日期分组聚合卡路里")
        void trend_groupedByDate() {
            createExercise(LocalDate.of(2026, 3, 10), 30, 300, runningType);
            createExercise(LocalDate.of(2026, 3, 10), 20, 200, swimmingType);
            createExercise(LocalDate.of(2026, 3, 11), 45, 360, runningType);
            entityManager.flush();

            List<Object[]> result = exerciseRepository.getCaloriesTrendByDateRange(
                    user.getId(), LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 11));

            assertThat(result).hasSize(2);
            assertThat(result).anyMatch(row ->
                    ((LocalDate) row[0]).equals(LocalDate.of(2026, 3, 10))
                            && ((Long) row[1]) == 500L);
            assertThat(result).anyMatch(row ->
                    ((LocalDate) row[0]).equals(LocalDate.of(2026, 3, 11))
                            && ((Long) row[1]) == 360L);
        }

        @Test
        @DisplayName("结果按日期升序排列")
        void trend_sortedByDate() {
            createExercise(LocalDate.of(2026, 3, 15), 30, 300, runningType);
            createExercise(LocalDate.of(2026, 3, 10), 45, 360, runningType);
            entityManager.flush();

            List<Object[]> result = exerciseRepository.getCaloriesTrendByDateRange(
                    user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

            LocalDate firstDate = (LocalDate) result.get(0)[0];
            LocalDate secondDate = (LocalDate) result.get(1)[0];
            assertThat(firstDate).isBefore(secondDate);
        }

        @Test
        @DisplayName("无记录时返回空列表")
        void trend_noRecords() {
            List<Object[]> result = exerciseRepository.getCaloriesTrendByDateRange(
                    user.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

            assertThat(result).isEmpty();
        }
    }
}
