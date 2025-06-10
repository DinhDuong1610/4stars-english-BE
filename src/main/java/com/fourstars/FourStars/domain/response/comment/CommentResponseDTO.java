package com.fourstars.FourStars.domain.response.comment;

import com.fourstars.FourStars.domain.response.post.PostResponseDTO.AttachmentInfoDTO;
import com.fourstars.FourStars.domain.response.post.PostResponseDTO.UserInfoDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class CommentResponseDTO {
    private long id;
    private String content;
    private UserInfoDTO user;
    private List<AttachmentInfoDTO> attachments;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CommentResponseDTO> replies;
}
