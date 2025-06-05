package com.fourstars.FourStars.domain.response.plan;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanResponseDTO {
    private long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationInDays;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
