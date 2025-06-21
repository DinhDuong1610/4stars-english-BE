package com.fourstars.FourStars.domain.response.notification;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

import com.fourstars.FourStars.util.constant.NotificationType;

@Getter
@Setter
public class NotificationResponseDTO {
    private Long id;
    private ActorDTO actor;
    private NotificationType type;
    private String message;
    private String link;
    private boolean isRead;
    private Instant createdAt;

    @Getter
    @Setter
    public static class ActorDTO {
        private Long id;
        private String name;
    }
}
