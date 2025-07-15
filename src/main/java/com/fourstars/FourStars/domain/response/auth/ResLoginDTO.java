package com.fourstars.FourStars.domain.response.auth;

import java.util.List;

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

        public UserLogin(long id, String email, String name, Role roleEntity, int streakCount) {
            this.id = id;
            this.email = email;
            this.name = name;
            if (roleEntity != null) {
                this.role = new RoleInfoDTO(roleEntity.getId(), roleEntity.getName(), roleEntity.getPermissions());
            }
            this.streakCount = streakCount;
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
}
