package com.sports.repository;

import com.sports.entity.Goal;
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
class GoalRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GoalRepository goalRepository;

    private User testUser;
    private LocalDate now;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);

        now = LocalDate.now();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_ShouldReturnGoalsInCorrectOrder() {
        Goal goal1 = createGoal("CALORIES", 1000, "ACTIVE", now.minusDays(7), now.plusDays(7));
        Goal goal2 = createGoal("DURATION", 300, "ACTIVE", now.minusDays(3), now.plusDays(3));
        Goal goal3 = createGoal("COUNT", 10, "COMPLETED", now.minusDays(14), now.minusDays(1));

        List<Goal> result = goalRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

        assertEquals(3, result.size());
        assertEquals(goal3.getId(), result.get(0).getId());
        assertEquals(goal2.getId(), result.get(1).getId());
        assertEquals(goal1.getId(), result.get(2).getId());
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_WithNoGoals_ShouldReturnEmptyList() {
        List<Goal> result = goalRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserIdAndStatus_ShouldFilterByStatus() {
        createGoal("CALORIES", 1000, "ACTIVE", now.minusDays(7), now.plusDays(7));
        createGoal("DURATION", 300, "ACTIVE", now.minusDays(3), now.plusDays(3));
        createGoal("COUNT", 10, "COMPLETED", now.minusDays(14), now.minusDays(1));
        createGoal("CALORIES", 500, "FAILED", now.minusDays(14), now.minusDays(7));

        List<Goal> activeGoals = goalRepository.findByUserIdAndStatus(testUser.getId(), "ACTIVE");
        List<Goal> completedGoals = goalRepository.findByUserIdAndStatus(testUser.getId(), "COMPLETED");
        List<Goal> failedGoals = goalRepository.findByUserIdAndStatus(testUser.getId(), "FAILED");

        assertEquals(2, activeGoals.size());
        assertEquals(1, completedGoals.size());
        assertEquals(1, failedGoals.size());
    }

    @Test
    void findByUserIdAndStatus_WithNoMatchingGoals_ShouldReturnEmptyList() {
        createGoal("CALORIES", 1000, "ACTIVE", now.minusDays(7), now.plusDays(7));

        List<Goal> result = goalRepository.findByUserIdAndStatus(testUser.getId(), "COMPLETED");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserIdAndEndDateBeforeAndStatus_ShouldFilterCorrectly() {
        createGoal("CALORIES", 1000, "ACTIVE", now.minusDays(14), now.minusDays(1));
        createGoal("DURATION", 300, "ACTIVE", now.minusDays(3), now.plusDays(3));
        createGoal("COUNT", 10, "ACTIVE", now.minusDays(21), now.minusDays(7));

        List<Goal> result = goalRepository.findByUserIdAndEndDateBeforeAndStatus(
                testUser.getId(), now, "ACTIVE");

        assertEquals(2, result.size());
    }

    @Test
    void findByUserIdAndEndDateBeforeAndStatus_WithWrongStatus_ShouldReturnEmpty() {
        createGoal("CALORIES", 1000, "COMPLETED", now.minusDays(14), now.minusDays(1));

        List<Goal> result = goalRepository.findByUserIdAndEndDateBeforeAndStatus(
                testUser.getId(), now, "ACTIVE");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_ShouldOnlyReturnUserGoals() {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword("password");
        otherUser.setEmail("other@example.com");
        entityManager.persist(otherUser);

        createGoal("CALORIES", 1000, "ACTIVE", now.minusDays(7), now.plusDays(7));
        Goal otherGoal = new Goal();
        otherGoal.setUser(otherUser);
        otherGoal.setGoalType("DURATION");
        otherGoal.setTargetValue(300);
        otherGoal.setCurrentValue(0);
        otherGoal.setStartDate(now.minusDays(7));
        otherGoal.setEndDate(now.plusDays(7));
        otherGoal.setStatus("ACTIVE");
        entityManager.persist(otherGoal);

        List<Goal> result = goalRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

        assertEquals(1, result.size());
        assertEquals("CALORIES", result.get(0).getGoalType());
    }

    @Test
    void saveGoal_ShouldPersistAllFields() {
        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setGoalType("CALORIES");
        goal.setTargetValue(2000);
        goal.setCurrentValue(500);
        goal.setStartDate(now.minusDays(7));
        goal.setEndDate(now.plusDays(7));
        goal.setTitle("Fitness Challenge");
        goal.setStatus("ACTIVE");

        Goal saved = goalRepository.save(goal);
        entityManager.flush();
        entityManager.clear();

        Goal found = goalRepository.findById(saved.getId()).orElseThrow();
        assertEquals("CALORIES", found.getGoalType());
        assertEquals(2000, found.getTargetValue());
        assertEquals(500, found.getCurrentValue());
        assertEquals(now.minusDays(7), found.getStartDate());
        assertEquals(now.plusDays(7), found.getEndDate());
        assertEquals("Fitness Challenge", found.getTitle());
        assertEquals("ACTIVE", found.getStatus());
    }

    @Test
    void getProgress_ShouldCalculateCorrectly() {
        Goal goal = createGoal("CALORIES", 1000, "ACTIVE", now.minusDays(7), now.plusDays(7));
        goal.setCurrentValue(500);

        assertEquals(50, goal.getProgress());
    }

    @Test
    void getProgress_WhenExceedsTarget_ShouldCapAt100() {
        Goal goal = createGoal("CALORIES", 1000, "ACTIVE", now.minusDays(7), now.plusDays(7));
        goal.setCurrentValue(1500);

        assertEquals(100, goal.getProgress());
    }

    @Test
    void getProgress_WhenTargetIsZero_ShouldReturnZero() {
        Goal goal = createGoal("CALORIES", 0, "ACTIVE", now.minusDays(7), now.plusDays(7));
        goal.setCurrentValue(500);

        assertEquals(0, goal.getProgress());
    }

    private Goal createGoal(String type, int target, String status, LocalDate start, LocalDate end) {
        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setGoalType(type);
        goal.setTargetValue(target);
        goal.setCurrentValue(0);
        goal.setStartDate(start);
        goal.setEndDate(end);
        goal.setStatus(status);
        return entityManager.persist(goal);
    }
}
