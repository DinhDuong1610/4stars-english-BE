package com.fourstars.FourStars.domain.response.category;

import com.fourstars.FourStars.util.constant.CategoryType;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class CategoryResponseDTO {
    private long id;
    private String name;
    private String description;
    private CategoryType type;
    private Integer orderIndex;
    private Long parentId;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private List<CategoryResponseDTO> subCategories;
}