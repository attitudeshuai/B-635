package com.sports.service;

import com.sports.dto.GoalRequest;
import com.sports.dto.GoalResponse;
import com.sports.entity.Goal;
import com.sports.entity.User;
import com.sports.exception.BusinessException;
import com.sports.repository.ExerciseRepository;
import com.sports.repository.GoalRepository;
import com.sports.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 目标服务
 */
@Service
public class GoalService {
    
    private static final Logger logger = LoggerFactory.getLogger(GoalService.class);
    
    @Autowired
    private GoalRepository goalRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ExerciseRepository exerciseRepository;
    
    public List<GoalResponse> getGoalsByUserId(Long userId) {
        List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);
        // 更新目标进度
        goals.forEach(this::updateGoalProgress);
        return goals.stream().map(this::toResponse).toList();
    }
    
    @Transactional
    public GoalResponse createGoal(Long userId, GoalRequest request) {
        logger.info("创建目标: userId={}, type={}", userId, request.getGoalType());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        
        Goal goal = new Goal();
        goal.setUser(user);
        goal.setGoalType(request.getGoalType());
        goal.setTargetValue(request.getTargetValue());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setTitle(request.getTitle());
        goal.setCurrentValue(0);
        goal.setStatus("ACTIVE");
        
        Goal saved = goalRepository.save(goal);
        updateGoalProgress(saved);
        
        logger.info("目标创建成功: id={}", saved.getId());
        return toResponse(saved);
    }
    
    @Transactional
    public GoalResponse updateGoal(Long userId, Long goalId, GoalRequest request) {
        logger.info("更新目标: userId={}, goalId={}", userId, goalId);
        
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new BusinessException("目标不存在"));
        
        if (!goal.getUser().getId().equals(userId)) {
            throw new BusinessException("无权操作此目标");
        }
        
        goal.setGoalType(request.getGoalType());
        goal.setTargetValue(request.getTargetValue());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setTitle(request.getTitle());
        
        Goal saved = goalRepository.save(goal);
        updateGoalProgress(saved);
        
        return toResponse(saved);
    }
    
    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        logger.info("删除目标: userId={}, goalId={}", userId, goalId);
        
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new BusinessException("目标不存在"));
        
        if (!goal.getUser().getId().equals(userId)) {
            throw new BusinessException("无权操作此目标");
        }
        
        goalRepository.delete(goal);
    }
    
    private void updateGoalProgress(Goal goal) {
        Long userId = goal.getUser().getId();
        LocalDate start = goal.getStartDate();
        LocalDate end = goal.getEndDate();
        
        Integer currentValue = 0;
        switch (goal.getGoalType()) {
            case "CALORIES":
                currentValue = exerciseRepository.sumCaloriesByUserIdAndDateRange(userId, start, end);
                break;
            case "DURATION":
                currentValue = exerciseRepository.sumDurationByUserIdAndDateRange(userId, start, end);
                break;
            case "COUNT":
                Long count = exerciseRepository.countByUserIdAndDateRange(userId, start, end);
                currentValue = count != null ? count.intValue() : 0;
                break;
        }
        
        goal.setCurrentValue(currentValue != null ? currentValue : 0);
        
        // 更新状态
        if (goal.getCurrentValue() >= goal.getTargetValue()) {
            goal.setStatus("COMPLETED");
        } else if (LocalDate.now().isAfter(goal.getEndDate())) {
            goal.setStatus("FAILED");
        }
        
        goalRepository.save(goal);
    }
    
    private GoalResponse toResponse(Goal goal) {
        GoalResponse response = new GoalResponse();
        response.setId(goal.getId());
        response.setGoalType(goal.getGoalType());
        response.setTargetValue(goal.getTargetValue());
        response.setCurrentValue(goal.getCurrentValue());
        response.setStartDate(goal.getStartDate());
        response.setEndDate(goal.getEndDate());
        response.setStatus(goal.getStatus());
        response.setTitle(goal.getTitle());
        response.setProgress(goal.getProgress());
        return response;
    }
}
