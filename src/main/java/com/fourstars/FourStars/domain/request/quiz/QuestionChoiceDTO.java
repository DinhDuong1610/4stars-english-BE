package com.fourstars.FourStars.domain.request.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionChoiceDTO {
    private long id;

    @NotBlank(message = "Choice content cannot be blank")
    private String content;

    private String imageUrl;

    @NotNull(message = "You must specify if the choice is correct.")
    private Boolean isCorrect;
}
