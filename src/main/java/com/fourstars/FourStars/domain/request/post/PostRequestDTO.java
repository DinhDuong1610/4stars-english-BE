package com.fourstars.FourStars.domain.request.post;

import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequestDTO {

    @Size(max = 65535, message = "Caption is too long")
    private String caption;

    private List<AttachmentRequestDTO> attachments;
}
