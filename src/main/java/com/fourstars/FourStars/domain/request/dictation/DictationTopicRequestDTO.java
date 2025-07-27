package com.fourstars.FourStars.domain.request.dictation;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DictationTopicRequestDTO {
    @NotBlank(message = "Title cannot be blank")
    private String title;

    private String description;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    @NotEmpty(message = "Sentences list cannot be empty")
    @Valid
    private List<DictationSentenceRequestDTO> sentences;
}