package com.fourstars.FourStars.domain.response.post;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class PostResponseDTO {
    private long id;
    private String caption;
    private boolean active;
    private UserInfoDTO user;
    private List<AttachmentInfoDTO> attachments;
    private long likeCount;
    private long commentCount;
    private boolean isLikedByCurrentUser;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @Setter
    public static class UserInfoDTO {
        private long id;
        private String name;
    }

    @Getter
    @Setter
    public static class AttachmentInfoDTO {
        private long id;
        private String fileUrl;
        private String fileType;
        private String originalFileName;
        private long fileSize;
    }
}
