package com.fourstars.FourStars.domain.response.dictation;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictationTopicResponseDTO {
    private long id;
    private String title;
    private String description;
    private CategoryInfoDTO category;
    private List<DictationSentenceResponseDTO> sentences;
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