package com.fourstars.FourStars.domain.request.comment;

import com.fourstars.FourStars.domain.request.post.AttachmentRequestDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CommentRequestDTO {

    @NotNull(message = "Post ID cannot be null")
    private Long postId;

    private Long parentCommentId;

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 65535, message = "Comment content is too long")
    private String content;

    private List<AttachmentRequestDTO> attachments;
}