package com.fourstars.FourStars.domain.response.grammar;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GrammarResponseDTO {
    private long id;
    private String name;
    private String content;
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
