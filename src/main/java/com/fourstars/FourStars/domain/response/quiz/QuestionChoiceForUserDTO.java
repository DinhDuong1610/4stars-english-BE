package com.fourstars.FourStars.domain.response.quiz;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionChoiceForUserDTO {
    private long id;
    private String content;
    private String imageUrl;
}