package com.fourstars.FourStars.domain.response.badge;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadgeResponseDTO {
    private long id;
    private String name;
    private String image;
    private Integer point;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
