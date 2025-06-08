package com.fourstars.FourStars.domain.request.video;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class VideoRequestDTO {

    @NotBlank(message = "Video title cannot be blank")
    @Size(max = 255, message = "Video title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Video URL cannot be blank")
    @URL(message = "URL is not valid")
    @Size(max = 2048, message = "Video URL cannot exceed 2048 characters")
    private String url;
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Size(max = 20, message = "Duration format is too long")
    private String duration;

    private String subtitle;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;
}
