package com.fourstars.FourStars.domain.response.quiz;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizForUserAttemptDTO {
    private long attemptId;
    private String quizTitle;
    private List<QuestionForUserDTO> questions;
}
