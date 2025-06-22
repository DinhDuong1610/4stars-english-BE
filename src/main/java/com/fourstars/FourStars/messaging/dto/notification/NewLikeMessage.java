package com.fourstars.FourStars.messaging.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewLikeMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long recipientId;
    private Long actorId;
    private Long postId;
}