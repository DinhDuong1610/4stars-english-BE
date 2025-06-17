package com.fourstars.FourStars.domain.response.quiz;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizForUserAttemptDTO {
    private long attemptId;
    private String quizTitle;
    private List<QuestionForUserDTO> questions;
}
