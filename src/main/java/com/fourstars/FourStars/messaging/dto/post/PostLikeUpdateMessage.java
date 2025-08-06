package com.fourstars.FourStars.messaging.dto.post;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeUpdateMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private long postId;
    private long totalLikes;
    private boolean isLikedByCurrentUser;
}