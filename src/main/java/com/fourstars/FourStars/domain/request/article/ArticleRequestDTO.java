package com.fourstars.FourStars.domain.request.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleRequestDTO {

    @NotBlank(message = "Article title cannot be blank")
    @Size(max = 255, message = "Article title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Article content cannot be blank")
    private String content;

    @Size(max = 2048, message = "Image URL is too long")
    private String image;

    @Size(max = 2048, message = "Audio URL is too long")
    private String audio;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;
}