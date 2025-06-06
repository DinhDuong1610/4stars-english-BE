package com.fourstars.FourStars.domain.response.subscription;

import java.time.Instant;

import com.fourstars.FourStars.util.constant.PaymentStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionResponseDTO {
    private long id;
    private UserInfoDTO user;
    private PlanInfoDTO plan;
    private Instant startDate;
    private Instant endDate;
    private boolean active;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @Setter
    public static class UserInfoDTO {
        private long id;
        private String name;
        private String email;
    }

    @Getter
    @Setter
    public static class PlanInfoDTO {
        private long id;
        private String name;
        private java.math.BigDecimal price;
        private Integer durationInDays;
    }
}
