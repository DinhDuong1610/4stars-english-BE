package com.fourstars.FourStars.domain.response.quiz;

import java.util.Set;

import com.fourstars.FourStars.util.constant.QuestionType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionForUserDTO {
    private long id;
    private QuestionType questionType;
    private String prompt;
    private String textToFill;
    private String audioUrl;
    private String imageUrl;
    private int points;
    private Set<QuestionChoiceForUserDTO> choices;
}
