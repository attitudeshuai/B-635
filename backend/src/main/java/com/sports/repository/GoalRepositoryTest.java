package com.sports.repository;

import com.sports.entity.Goal;
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
class GoalRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GoalRepository goalRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("goaluser_repo");
        user.setPassword("password");
        user.setEmail("goalrepo@test.com");
        entityManager.persist(user);
        entityManager.flush();
    }

    private Goal createGoal(String goalType, int targetValue, String status, LocalDate startDate, LocalDate endDate) {
        Goal goal = new Goal();
        goal.setUser(user);
        goal.setGoalType(goalType);
        goal.setTargetValue(targetValue);
        goal.setCurrentValue(0);
        goal.setStartDate(startDate);
        goal.setEndDate(endDate);
        goal.setStatus(status);
        goal.setTitle(goalType + "目标");
        entityManager.persist(goal);
        return goal;
    }

    @Nested
    @DisplayName("findByUserIdOrderByCreatedAtDesc")
    class FindByUserIdOrderedTests {

        @Test
        @DisplayName("按创建时间降序返回目标")
        void orderedByCreatedAtDesc() throws InterruptedException {
            Goal g1 = createGoal("CALORIES", 1000, "ACTIVE",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
            entityManager.flush();
            Thread.sleep(10);
            Goal g2 = createGoal("DURATION", 300, "COMPLETED",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
            entityManager.flush();

            List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

            assertThat(goals).hasSize(2);
            assertThat(goals.get(0).getId()).isEqualTo(g2.getId());
            assertThat(goals.get(1).getId()).isEqualTo(g1.getId());
        }

        @Test
        @DisplayName("无目标时返回空列表")
        void noGoals() {
            List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

            assertThat(goals).isEmpty();
        }

        @Test
        @DisplayName("只返回指定用户的目标")
        void onlyOwnGoals() {
            User otherUser = new User();
            otherUser.setUsername("othergoaluser");
            otherUser.setPassword("password");
            otherUser.setEmail("othergoal@test.com");
            entityManager.persist(otherUser);

            createGoal("CALORIES", 1000, "ACTIVE",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

            Goal otherGoal = new Goal();
            otherGoal.setUser(otherUser);
            otherGoal.setGoalType("COUNT");
            otherGoal.setTargetValue(10);
            otherGoal.setCurrentValue(0);
            otherGoal.setStartDate(LocalDate.of(2026, 1, 1));
            otherGoal.setEndDate(LocalDate.of(2026, 12, 31));
            otherGoal.setStatus("ACTIVE");
            otherGoal.setTitle("他人目标");
            entityManager.persist(otherGoal);
            entityManager.flush();

            List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

            assertThat(goals).hasSize(1);
            assertThat(goals.get(0).getGoalType()).isEqualTo("CALORIES");
        }
    }

    @Nested
    @DisplayName("findByUserIdAndStatus")
    class FindByUserIdAndStatusTests {

        @Test
        @DisplayName("按状态过滤目标-ACTIVE")
        void filterByActive() {
            createGoal("CALORIES", 1000, "ACTIVE",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
            createGoal("DURATION", 300, "COMPLETED",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
            createGoal("COUNT", 10, "ACTIVE",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
            entityManager.flush();

            List<Goal> activeGoals = goalRepository.findByUserIdAndStatus(user.getId(), "ACTIVE");

            assertThat(activeGoals).hasSize(2);
            assertThat(activeGoals).allMatch(g -> g.getStatus().equals("ACTIVE"));
        }

        @Test
        @DisplayName("按状态过滤目标-COMPLETED")
        void filterByCompleted() {
            createGoal("CALORIES", 1000, "ACTIVE",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
            createGoal("DURATION", 300, "COMPLETED",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
            entityManager.flush();

            List<Goal> completedGoals = goalRepository.findByUserIdAndStatus(user.getId(), "COMPLETED");

            assertThat(completedGoals).hasSize(1);
            assertThat(completedGoals.get(0).getGoalType()).isEqualTo("DURATION");
        }

        @Test
        @DisplayName("按状态过滤目标-FAILED")
        void filterByFailed() {
            createGoal("COUNT", 10, "FAILED",
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
            entityManager.flush();

            List<Goal> failedGoals = goalRepository.findByUserIdAndStatus(user.getId(), "FAILED");

            assertThat(failedGoals).hasSize(1);
            assertThat(failedGoals.get(0).getStatus()).isEqualTo("FAILED");
        }

        @Test
        @DisplayName("无匹配状态时返回空列表")
        void noMatchingStatus() {
            createGoal("CALORIES", 1000, "ACTIVE",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
            entityManager.flush();

            List<Goal> failedGoals = goalRepository.findByUserIdAndStatus(user.getId(), "FAILED");

            assertThat(failedGoals).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserIdAndEndDateBeforeAndStatus")
    class FindExpiredGoalsTests {

        @Test
        @DisplayName("查找过期且仍在ACTIVE状态的目标")
        void findExpiredActiveGoals() {
            createGoal("CALORIES", 1000, "ACTIVE",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
            createGoal("DURATION", 300, "COMPLETED",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
            createGoal("COUNT", 10, "ACTIVE",
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 12, 31));
            entityManager.flush();

            List<Goal> expired = goalRepository.findByUserIdAndEndDateBeforeAndStatus(
                    user.getId(), LocalDate.of(2026, 6, 1), "ACTIVE");

            assertThat(expired).hasSize(1);
            assertThat(expired.get(0).getGoalType()).isEqualTo("CALORIES");
        }

        @Test
        @DisplayName("无过期目标时返回空列表")
        void noExpiredGoals() {
            createGoal("CALORIES", 1000, "ACTIVE",
                    LocalDate.of(2026, 6, 1), LocalDate.of(2026, 12, 31));
            entityManager.flush();

            List<Goal> expired = goalRepository.findByUserIdAndEndDateBeforeAndStatus(
                    user.getId(), LocalDate.of(2026, 6, 1), "ACTIVE");

            assertThat(expired).isEmpty();
        }

        @Test
        @DisplayName("endDate刚好等于查询日期的不算过期(Before不含边界)")
        void boundaryDate() {
            createGoal("CALORIES", 1000, "ACTIVE",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1));
            entityManager.flush();

            List<Goal> expired = goalRepository.findByUserIdAndEndDateBeforeAndStatus(
                    user.getId(), LocalDate.of(2026, 6, 1), "ACTIVE");

            assertThat(expired).isEmpty();
        }
    }
}
