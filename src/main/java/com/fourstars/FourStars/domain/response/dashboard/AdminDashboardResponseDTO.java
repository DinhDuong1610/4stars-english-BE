package com.fourstars.FourStars.domain.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AdminDashboardResponseDTO {
    private long totalUsers;
    private long newUsersThisMonth;
    private long totalActiveSubscriptions;
    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    private Map<String, Long> contentCount;

    private List<ChartDataPoint> newUserRegistrationsChart;
    private List<ChartDataPoint> premiumUpgradesChart;
    private List<ChartDataPoint> revenueChart;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ChartDataPoint {
        private String date;
        private Number value;
    }
}