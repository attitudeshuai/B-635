package com.sports.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 运动类型实体类
 */
@Entity
@Table(name = "exercise_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(length = 50)
    private String icon;
    
    @Column(name = "calories_per_minute")
    private Integer caloriesPerMinute;
    
    @Column(length = 255)
    private String description;
    
    @Column(length = 20)
    private String color;
}
