package com.sports.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 目标响应DTO
 */
@Data
public class GoalResponse {
    private Long id;
    private String goalType;
    private Integer targetValue;
    private Integer currentValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String title;
    private Integer progress;
}
