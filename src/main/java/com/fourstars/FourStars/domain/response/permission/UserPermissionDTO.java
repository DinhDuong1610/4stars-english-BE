package com.fourstars.FourStars.domain.response.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roleName;
    private Set<String> permissions;
}
