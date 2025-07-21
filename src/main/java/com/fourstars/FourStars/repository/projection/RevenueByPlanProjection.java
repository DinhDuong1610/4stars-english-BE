package com.fourstars.FourStars.repository.projection;

import java.math.BigDecimal;

public interface RevenueByPlanProjection {
    Long getPlanId();

    String getPlanName();

    Long getTransactionCount();

    BigDecimal getTotalRevenue();
}
