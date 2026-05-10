package com.sports.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 运动记录响应DTO
 */
@Data
public class ExerciseResponse {
    private Long id;
    private Long typeId;
    private String typeName;
    private String typeIcon;
    private String typeColor;
    private Integer durationMinutes;
    private Integer caloriesBurned;
    private BigDecimal distanceKm;
    private LocalDate exerciseDate;
    private LocalTime startTime;
    private String notes;
}
