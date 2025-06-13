package com.fourstars.FourStars.domain.request.quiz;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAnswerRequestDTO {
    @NotNull
    private Long questionId;
    private String userAnswerText;
    private Long selectedChoiceId;
}
