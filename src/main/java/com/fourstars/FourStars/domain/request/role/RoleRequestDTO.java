package com.fourstars.FourStars.domain.request.role;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleRequestDTO {
    @NotBlank(message = "Role name cannot be blank")
    @Size(max = 100, message = "Role name cannot exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private boolean active = true;

    private List<Long> permissionIds;
}
