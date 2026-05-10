package com.sports.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 运动记录实体类
 */
@Entity
@Table(name = "exercises")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exercise {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id", nullable = false)
    private ExerciseType exerciseType;
    
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    @Column(name = "calories_burned")
    private Integer caloriesBurned;
    
    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;
    
    @Column(name = "exercise_date", nullable = false)
    private LocalDate exerciseDate;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(length = 500)
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // 自动计算卡路里消耗
        if (caloriesBurned == null && exerciseType != null && exerciseType.getCaloriesPerMinute() != null) {
            caloriesBurned = durationMinutes * exerciseType.getCaloriesPerMinute();
        }
    }
}
