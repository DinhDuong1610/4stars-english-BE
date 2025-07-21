package com.fourstars.FourStars.domain.response.statistic;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RevenueStatsDTO {
    private String startDate;
    private String endDate;
    private BigDecimal totalRevenue;
    private long totalTransactions;
    private List<RevenueByPlanDTO> revenueByPlan;
}