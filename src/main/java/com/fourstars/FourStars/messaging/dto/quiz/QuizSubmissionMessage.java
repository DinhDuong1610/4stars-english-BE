package com.fourstars.FourStars.messaging.dto.quiz;

import java.io.Serializable;
import java.util.List;

import com.fourstars.FourStars.domain.request.quiz.UserAnswerRequestDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId;
    private Long userQuizAttemptId;
    private List<UserAnswerRequestDTO> answers;
}
