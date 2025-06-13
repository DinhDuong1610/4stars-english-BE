package com.fourstars.FourStars.domain.response.quiz;

import com.fourstars.FourStars.domain.request.quiz.QuestionChoiceDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionAnswerDetailDTO {
    private String correctText;
    private QuestionChoiceDTO correctChoice;
}
