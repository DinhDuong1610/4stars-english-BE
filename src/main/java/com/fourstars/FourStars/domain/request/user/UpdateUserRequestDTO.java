package com.fourstars.FourStars.domain.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequestDTO {

    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Email(message = "Email is not valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    private Boolean active;

    @Min(value = 0, message = "Point must be greater than or equal to 0")
    private Integer point;

    private Long roleId;
    private Long badgeId;

}
