package com.fourstars.FourStars.messaging.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long recipientId;
    private Long attemptId;
    private String quizTitle;
    private int score;
}
