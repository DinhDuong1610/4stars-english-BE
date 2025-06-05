package com.fourstars.FourStars.domain.request.plan;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanRequestDTO {
    @NotBlank(message = "Plan name cannot be blank")
    @Size(max = 150, message = "Plan name cannot exceed 150 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;

    @NotNull(message = "Duration in days cannot be null")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationInDays;

    private boolean active = true;
}
