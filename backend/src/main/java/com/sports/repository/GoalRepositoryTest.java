package com.sports.repository;

import com.sports.entity.Goal;
import com.sports.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("GoalRepository 单元测试")
class GoalRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GoalRepository goalRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("t***@***********");
        entityManager.persist(user);
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc - 按创建时间降序查询用户目标")
    void testFindByUserIdOrderByCreatedAtDesc() {
        Goal goal1 = createGoal(user, "CALORIES", 1000, LocalDate.now().minusDays(10));
        Goal goal2 = createGoal(user, "DURATION", 300, LocalDate.now().minusDays(5));
        Goal goal3 = createGoal(user, "COUNT", 10, LocalDate.now());

        List<Goal> result = goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        assertEquals(3, result.size());
        assertEquals(goal3.getId(), result.get(0).getId());
        assertEquals(goal2.getId(), result.get(1).getId());
        assertEquals(goal1.getId(), result.get(2).getId());
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc - 无数据时返回空列表")
    void testFindByUserIdOrderByCreatedAtDesc_NoData() {
        List<Goal> result = goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByUserIdAndStatus - 按状态筛选目标")
    void testFindByUserIdAndStatus() {
        createGoalWithStatus(user, "CALORIES", 1000, "ACTIVE");
        createGoalWithStatus(user, "DURATION", 300, "COMPLETED");
        createGoalWithStatus(user, "COUNT", 10, "ACTIVE");
        createGoalWithStatus(user, "CALORIES", 500, "FAILED");

        List<Goal> activeGoals = goalRepository.findByUserIdAndStatus(user.getId(), "ACTIVE");
        List<Goal> completedGoals = goalRepository.findByUserIdAndStatus(user.getId(), "COMPLETED");
        List<Goal> failedGoals = goalRepository.findByUserIdAndStatus(user.getId(), "FAILED");

        assertEquals(2, activeGoals.size());
        assertEquals(1, completedGoals.size());
        assertEquals(1, failedGoals.size());
    }

    @Test
    @DisplayName("findByUserIdAndStatus - 无匹配状态返回空列表")
    void testFindByUserIdAndStatus_NoMatch() {
        createGoalWithStatus(user, "CALORIES", 1000, "ACTIVE");

        List<Goal> result = goalRepository.findByUserIdAndStatus(user.getId(), "COMPLETED");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("字段契约验证 - 确保实体字段正确映射")
    void testFieldMapping() {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 31);
        Goal goal = createGoal(user, "CALORIES", 1000, startDate, endDate);
        goal.setCurrentValue(500);
        goal.setTitle("五月份减脂目标");
        entityManager.persist(goal);
        entityManager.flush();
        entityManager.clear();

        List<Goal> result = goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        Goal saved = result.get(0);

        assertNotNull(saved.getId());
        assertEquals("CALORIES", saved.getGoalType());
        assertEquals(Integer.valueOf(1000), saved.getTargetValue());
        assertEquals(Integer.valueOf(500), saved.getCurrentValue());
        assertEquals("五月份减脂目标", saved.getTitle());
        assertEquals(startDate, saved.getStartDate());
        assertEquals(endDate, saved.getEndDate());
        assertEquals("ACTIVE", saved.getStatus());
        assertEquals(user.getId(), saved.getUser().getId());
    }

    @Test
    @DisplayName("getProgress 计算 - 正常百分比计算")
    void testGetProgress() {
        Goal goal = new Goal();
        goal.setTargetValue(1000);
        goal.setCurrentValue(500);

        assertEquals(50, goal.getProgress());
    }

    @Test
    @DisplayName("getProgress 计算 - 进度上限为 100")
    void testGetProgress_CapAt100() {
        Goal goal = new Goal();
        goal.setTargetValue(1000);
        goal.setCurrentValue(1500);

        assertEquals(100, goal.getProgress());
    }

    @Test
    @DisplayName("getProgress 计算 - targetValue 为 null 返回 0")
    void testGetProgress_NullTargetValue() {
        Goal goal = new Goal();
        goal.setTargetValue(null);
        goal.setCurrentValue(500);

        assertEquals(0, goal.getProgress());
    }

    @Test
    @DisplayName("getProgress 计算 - targetValue 为 0 返回 0")
    void testGetProgress_ZeroTargetValue() {
        Goal goal = new Goal();
        goal.setTargetValue(0);
        goal.setCurrentValue(500);

        assertEquals(0, goal.getProgress());
    }

    @Test
    @DisplayName("多用户隔离 - 只返回当前用户的目标")
    void testUserIsolation() {
        User otherUser = new User();
        otherUser.setUsername("other");
        otherUser.setPassword("password");
        otherUser.setEmail("o****@***********");
        entityManager.persist(otherUser);

        createGoal(user, "CALORIES", 1000, LocalDate.now());
        createGoal(user, "DURATION", 300, LocalDate.now());
        createGoal(otherUser, "COUNT", 10, LocalDate.now());

        List<Goal> userGoals = goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<Goal> otherGoals = goalRepository.findByUserIdOrderByCreatedAtDesc(otherUser.getId());

        assertEquals(2, userGoals.size());
        assertEquals(1, otherGoals.size());
    }

    private Goal createGoal(User user, String type, int target, LocalDate createdAt) {
        return createGoal(user, type, target, createdAt, createdAt.plusDays(30));
    }

    private Goal createGoal(User user, String type, int target, LocalDate startDate, LocalDate endDate) {
        Goal goal = new Goal();
        goal.setUser(user);
        goal.setGoalType(type);
        goal.setTargetValue(target);
        goal.setStartDate(startDate);
        goal.setEndDate(endDate);
        goal.setStatus("ACTIVE");
        entityManager.persist(goal);
        return goal;
    }

    private Goal createGoalWithStatus(User user, String type, int target, String status) {
        Goal goal = new Goal();
        goal.setUser(user);
        goal.setGoalType(type);
        goal.setTargetValue(target);
        goal.setStartDate(LocalDate.now());
        goal.setEndDate(LocalDate.now().plusDays(30));
        goal.setStatus(status);
        entityManager.persist(goal);
        return goal;
    }
}
