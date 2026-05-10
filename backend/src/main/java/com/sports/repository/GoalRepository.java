package com.sports.repository;

import com.sports.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * 运动目标数据访问接口
 */
@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    
    List<Goal> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Goal> findByUserIdAndStatus(Long userId, String status);
    
    List<Goal> findByUserIdAndEndDateBeforeAndStatus(Long userId, LocalDate date, String status);
}
