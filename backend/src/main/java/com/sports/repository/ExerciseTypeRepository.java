package com.sports.repository;

import com.sports.entity.ExerciseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 运动类型数据访问接口
 */
@Repository
public interface ExerciseTypeRepository extends JpaRepository<ExerciseType, Long> {
}
