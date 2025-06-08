package com.fourstars.FourStars.domain.request.vocabulary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VocabularyRequestDTO {

    @NotBlank(message = "Word cannot be blank")
    @Size(max = 150, message = "Word cannot exceed 150 characters")
    private String word;

    @Size(max = 10000, message = "English definition is too long")
    private String definitionEn;

    @Size(max = 10000, message = "Vietnamese meaning is too long")
    private String meaningVi;

    @Size(max = 10000, message = "English example is too long")
    private String exampleEn;

    @Size(max = 10000, message = "Vietnamese example is too long")
    private String exampleVi;

    @Size(max = 50, message = "Part of speech is too long")
    private String partOfSpeech;

    @Size(max = 100, message = "Pronunciation is too long")
    private String pronunciation;

    @Size(max = 2048, message = "Image URL is too long")
    private String image;

    @Size(max = 2048, message = "Audio URL is too long")
    private String audio;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;
}
