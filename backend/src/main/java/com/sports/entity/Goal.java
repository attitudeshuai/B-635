package com.sports.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运动目标实体类
 */
@Entity
@Table(name = "goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "goal_type", nullable = false, length = 50)
    private String goalType; // CALORIES, DURATION, COUNT
    
    @Column(name = "target_value", nullable = false)
    private Integer targetValue;
    
    @Column(name = "current_value")
    private Integer currentValue = 0;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, FAILED
    
    @Column(length = 100)
    private String title;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (currentValue == null) currentValue = 0;
        if (status == null) status = "ACTIVE";
    }
    
    public int getProgress() {
        if (targetValue == null || targetValue == 0) return 0;
        return Math.min(100, (currentValue * 100) / targetValue);
    }
}
