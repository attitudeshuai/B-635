package com.sports.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 运动记录请求DTO
 */
@Data
public class ExerciseRequest {
    
    @NotNull(message = "运动类型不能为空")
    private Long typeId;
    
    @NotNull(message = "运动时长不能为空")
    @Positive(message = "运动时长必须为正数")
    private Integer durationMinutes;
    
    private BigDecimal distanceKm;
    
    @NotNull(message = "运动日期不能为空")
    private LocalDate exerciseDate;
    
    private LocalTime startTime;
    
    private String notes;
}
