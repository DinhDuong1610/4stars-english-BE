package com.fourstars.FourStars.domain.request.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
public class QuizDTO {
    private long id;

    @NotBlank(message = "Quiz title cannot be blank")
    @Size(max = 255)
    private String title;

    private String description;

    private Long categoryId;

    private String categoryName;

    private Instant createdAt;

    private Instant updatedAt;

    private Set<QuestionDTO> questions;
}
