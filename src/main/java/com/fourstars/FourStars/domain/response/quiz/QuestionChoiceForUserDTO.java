package com.fourstars.FourStars.domain.response.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionChoiceForUserDTO {
    private long id;
    private String content;
    private String imageUrl;
}