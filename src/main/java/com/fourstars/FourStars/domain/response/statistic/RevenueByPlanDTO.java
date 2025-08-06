package com.fourstars.FourStars.domain.response.statistic;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RevenueByPlanDTO {
    private long planId;
    private String planName;
    private long transactionCount;
    private BigDecimal totalRevenue;
}
