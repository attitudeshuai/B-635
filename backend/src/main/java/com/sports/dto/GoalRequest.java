package com.sports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

/**
 * 目标请求DTO
 */
@Data
public class GoalRequest {
    
    @NotBlank(message = "目标类型不能为空")
    private String goalType; // CALORIES, DURATION, COUNT
    
    @NotNull(message = "目标值不能为空")
    @Positive(message = "目标值必须为正数")
    private Integer targetValue;
    
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
    
    private String title;
}
