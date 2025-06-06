package com.fourstars.FourStars.domain.request.category;

import com.fourstars.FourStars.util.constant.CategoryType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequestDTO {

    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 150, message = "Category name cannot exceed 150 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Category type cannot be null")
    @Enumerated(EnumType.STRING)
    private CategoryType type;

    @NotNull(message = "Order index cannot be null")
    @Min(value = 0, message = "Order index must be non-negative")
    private Integer orderIndex = 0;

    private Long parentId;
}
