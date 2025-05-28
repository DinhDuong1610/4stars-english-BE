package com.fourstars.FourStars.domain.response.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDTO {
    private long id;
    private String name;
    private String apiPath;
    private String method;
    private String module;
}
