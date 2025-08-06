package com.fourstars.FourStars.domain.response.user;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
    private long id;
    private String name;
    private String email;
    private boolean active;
    private int point;
    private int streakCount;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private RoleInfoDTO role;
    private BadgeInfoDTO badge;

    @Getter
    @Setter
    public static class RoleInfoDTO {
        private long id;
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BadgeInfoDTO {
        private long id;
        private String name;
        private String image;
    }
}
