package com.fourstars.FourStars.domain.response.quiz;

import java.time.Instant;
import java.util.List;

import com.fourstars.FourStars.util.constant.QuizStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAttemptResponseDTO {
    private long id;
    private long quizId;
    private String quizTitle;
    private QuizStatus status;
    private int score;
    private int totalPoints;
    private Instant startedAt;
    private Instant completedAt;
    private List<UserAnswerResponseDTO> userAnswers;
}
