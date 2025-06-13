package com.fourstars.FourStars.domain.request.quiz;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitQuizRequestDTO {
    @NotNull
    private Long userQuizAttemptId;
    @NotEmpty
    @Valid
    private List<UserAnswerRequestDTO> answers;
}
