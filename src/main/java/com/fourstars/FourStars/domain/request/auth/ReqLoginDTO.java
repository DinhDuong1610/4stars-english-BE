package com.fourstars.FourStars.domain.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqLoginDTO {

    @NotBlank(message = "Username (email) cannot be blank")
    @Email(message = "Username must be a valid email")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}