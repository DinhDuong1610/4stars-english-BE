package com.fourstars.FourStars.domain.request.quiz;

import com.fourstars.FourStars.util.constant.QuestionType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class QuestionDTO {
    private long id;

    @NotNull(message = "Question type cannot be null")
    private QuestionType questionType;

    @NotBlank(message = "Question prompt cannot be blank")
    private String prompt;

    private String textToFill;
    private String correctSentence;
    private String audioUrl;
    private String imageUrl;
    private int points = 10;
    private int questionOrder;
    private Long relatedVocabularyId;

    @Valid
    @NotEmpty(message = "Multiple choice questions must have choices.")
    private Set<QuestionChoiceDTO> choices;
}
