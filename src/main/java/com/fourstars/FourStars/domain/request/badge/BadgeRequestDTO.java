package com.fourstars.FourStars.domain.request.badge;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadgeRequestDTO {
    @NotBlank(message = "Badge name cannot be blank")
    @Size(max = 100, message = "Badge name cannot exceed 100 characters")
    private String name;

    @Size(max = 2048, message = "Image URL is too long")
    private String image;

    @NotNull(message = "Point value for the badge cannot be null")
    @Min(value = 0, message = "Point value must be non-negative")
    private Integer point = 0;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
