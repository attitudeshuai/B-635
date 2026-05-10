package com.sports.repository;

import com.sports.entity.Exercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * 运动记录数据访问接口
 */
@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    
    Page<Exercise> findByUserIdOrderByExerciseDateDesc(Long userId, Pageable pageable);
    
    List<Exercise> findByUserIdAndExerciseDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(e.caloriesBurned) FROM Exercise e WHERE e.user.id = :userId AND e.exerciseDate BETWEEN :startDate AND :endDate")
    Integer sumCaloriesByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(e.durationMinutes) FROM Exercise e WHERE e.user.id = :userId AND e.exerciseDate BETWEEN :startDate AND :endDate")
    Integer sumDurationByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(e) FROM Exercise e WHERE e.user.id = :userId AND e.exerciseDate BETWEEN :startDate AND :endDate")
    Long countByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT e.exerciseType.name, COUNT(e) FROM Exercise e WHERE e.user.id = :userId GROUP BY e.exerciseType.name")
    List<Object[]> countByExerciseType(@Param("userId") Long userId);
    
    @Query("SELECT e.exerciseDate, SUM(e.caloriesBurned) FROM Exercise e WHERE e.user.id = :userId AND e.exerciseDate BETWEEN :startDate AND :endDate GROUP BY e.exerciseDate ORDER BY e.exerciseDate")
    List<Object[]> getCaloriesTrendByDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
