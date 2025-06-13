package com.fourstars.FourStars.domain.response.quiz;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAnswerResponseDTO {
    private long questionId;
    private String questionPrompt;
    private String userAnswerText;
    private Long selectedChoiceId;
    private boolean isCorrect;
    private int pointsAwarded;
    private QuestionAnswerDetailDTO correctAnswer;
}
