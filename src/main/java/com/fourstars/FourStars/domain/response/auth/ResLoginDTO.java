package com.fourstars.FourStars.domain.response.auth;

import java.util.List;

import com.fourstars.FourStars.domain.Badge;
import com.fourstars.FourStars.domain.Permission;
import com.fourstars.FourStars.domain.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResLoginDTO {
    private String accessToken;
    private UserLogin user;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserLogin {
        private long id;
        private String email;
        private String name;
        private RoleInfoDTO role;
        private int streakCount;
        private int point;
        private BadgeInfoDTO badge;

        public UserLogin(long id, String email, String name, Role roleEntity, int streakCount, int point,
                Badge badge) {
            this.id = id;
            this.email = email;
            this.name = name;
            if (roleEntity != null) {
                this.role = new RoleInfoDTO(roleEntity.getId(), roleEntity.getName(), roleEntity.getPermissions());
            }
            this.streakCount = streakCount;
            this.point = point;
            if (badge != null) {
                this.badge = new BadgeInfoDTO(badge.getId(), badge.getName(), badge.getImage());
            }
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInsideToken {
        private long id;
        private String email;
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfoDTO {
        private long id;
        private String name;
        private List<Permission> permissions;
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
