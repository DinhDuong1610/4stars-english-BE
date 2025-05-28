package com.fourstars.FourStars.domain.response.role;

import java.time.Instant;
import java.util.List;

import com.fourstars.FourStars.domain.response.permission.PermissionResponseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleResponseDTO {
    private long id;
    private String name;
    private String description;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<PermissionResponseDTO> permissions;
}
