package com.fourstars.FourStars.domain.response.video;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class VideoResponseDTO {
    private long id;
    private String title;
    private String url;
    private String description;
    private String duration;
    private String subtitle;
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
