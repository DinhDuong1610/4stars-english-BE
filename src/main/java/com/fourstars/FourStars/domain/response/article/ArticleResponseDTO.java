package com.fourstars.FourStars.domain.response.article;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class ArticleResponseDTO {
    private long id;
    private String title;
    private String content;
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
