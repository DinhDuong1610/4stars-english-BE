package com.fourstars.FourStars.domain.request.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequestDTO {
    private String code;
}