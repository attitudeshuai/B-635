package com.sports.service;

import com.sports.dto.ExerciseRequest;
import com.sports.dto.ExerciseResponse;
import com.sports.entity.Exercise;
import com.sports.entity.ExerciseType;
import com.sports.entity.User;
import com.sports.exception.BusinessException;
import com.sports.repository.ExerciseRepository;
import com.sports.repository.ExerciseTypeRepository;
import com.sports.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 运动记录服务
 */
@Service
public class ExerciseService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExerciseService.class);
    
    @Autowired
    private ExerciseRepository exerciseRepository;
    
    @Autowired
    private ExerciseTypeRepository exerciseTypeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Page<ExerciseResponse> getExercisesByUserId(Long userId, int page, int size) {
        Page<Exercise> exercises = exerciseRepository.findByUserIdOrderByExerciseDateDesc(
                userId, PageRequest.of(page, size));
        return exercises.map(this::toResponse);
    }
    
    public List<ExerciseResponse> getExercisesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Exercise> exercises = exerciseRepository.findByUserIdAndExerciseDateBetween(userId, startDate, endDate);
        return exercises.stream().map(this::toResponse).toList();
    }
    
    public List<ExerciseType> getAllExerciseTypes() {
        return exerciseTypeRepository.findAll();
    }
    
    @Transactional
    public ExerciseResponse createExercise(Long userId, ExerciseRequest request) {
        logger.info("创建运动记录: userId={}, typeId={}", userId, request.getTypeId());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        ExerciseType type = exerciseTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new BusinessException("运动类型不存在"));
        
        Exercise exercise = new Exercise();
        exercise.setUser(user);
        exercise.setExerciseType(type);
        exercise.setDurationMinutes(request.getDurationMinutes());
        exercise.setDistanceKm(request.getDistanceKm());
        exercise.setExerciseDate(request.getExerciseDate());
        exercise.setStartTime(request.getStartTime());
        exercise.setNotes(request.getNotes());
        
        // 自动计算卡路里
        if (type.getCaloriesPerMinute() != null) {
            exercise.setCaloriesBurned(request.getDurationMinutes() * type.getCaloriesPerMinute());
        }
        
        Exercise saved = exerciseRepository.save(exercise);
        logger.info("运动记录创建成功: id={}", saved.getId());
        
        return toResponse(saved);
    }
    
    @Transactional
    public ExerciseResponse updateExercise(Long userId, Long exerciseId, ExerciseRequest request) {
        logger.info("更新运动记录: userId={}, exerciseId={}", userId, exerciseId);
        
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new BusinessException("运动记录不存在"));
        
        if (!exercise.getUser().getId().equals(userId)) {
            throw new BusinessException("无权操作此记录");
        }
        
        ExerciseType type = exerciseTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new BusinessException("运动类型不存在"));
        
        exercise.setExerciseType(type);
        exercise.setDurationMinutes(request.getDurationMinutes());
        exercise.setDistanceKm(request.getDistanceKm());
        exercise.setExerciseDate(request.getExerciseDate());
        exercise.setStartTime(request.getStartTime());
        exercise.setNotes(request.getNotes());
        
        if (type.getCaloriesPerMinute() != null) {
            exercise.setCaloriesBurned(request.getDurationMinutes() * type.getCaloriesPerMinute());
        }
        
        Exercise saved = exerciseRepository.save(exercise);
        return toResponse(saved);
    }
    
    @Transactional
    public void deleteExercise(Long userId, Long exerciseId) {
        logger.info("删除运动记录: userId={}, exerciseId={}", userId, exerciseId);
        
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new BusinessException("运动记录不存在"));
        
        if (!exercise.getUser().getId().equals(userId)) {
            throw new BusinessException("无权操作此记录");
        }
        
        exerciseRepository.delete(exercise);
    }
    
    private ExerciseResponse toResponse(Exercise exercise) {
        ExerciseResponse response = new ExerciseResponse();
        response.setId(exercise.getId());
        response.setTypeId(exercise.getExerciseType().getId());
        response.setTypeName(exercise.getExerciseType().getName());
        response.setTypeIcon(exercise.getExerciseType().getIcon());
        response.setTypeColor(exercise.getExerciseType().getColor());
        response.setDurationMinutes(exercise.getDurationMinutes());
        response.setCaloriesBurned(exercise.getCaloriesBurned());
        response.setDistanceKm(exercise.getDistanceKm());
        response.setExerciseDate(exercise.getExerciseDate());
        response.setStartTime(exercise.getStartTime());
        response.setNotes(exercise.getNotes());
        return response;
    }
}
