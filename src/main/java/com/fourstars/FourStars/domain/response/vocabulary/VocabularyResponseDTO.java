package com.fourstars.FourStars.domain.response.vocabulary;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class VocabularyResponseDTO {
    private long id;
    private String word;
    private String definitionEn;
    private String meaningVi;
    private String exampleEn;
    private String exampleVi;
    private String partOfSpeech;
    private String pronunciation;
    private String image;
    private String audio;
    private CategoryInfoDTO category;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @Getter
    @Setter
    public static class CategoryInfoDTO {
        private long id;
        private String name;
    }
}
