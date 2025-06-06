package com.fourstars.FourStars.domain.request.subscription;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionRequestDTO {

    @NotNull(message = "Plan ID cannot be null")
    private Long planId;
}
