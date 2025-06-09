package com.fourstars.FourStars.domain.request.post;

import org.hibernate.validator.constraints.URL;

import com.fourstars.FourStars.util.constant.AttachmentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachmentRequestDTO {

    @NotBlank(message = "File URL cannot be blank")
    @URL(message = "URL is not valid")
    @Size(max = 2048, message = "File URL is too long")
    private String fileUrl;

    @NotNull(message = "File type cannot be null")
    private AttachmentType fileType;
}
