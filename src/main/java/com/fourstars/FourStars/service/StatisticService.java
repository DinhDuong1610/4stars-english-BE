package com.fourstars.FourStars.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.response.statistic.RevenueByPlanDTO;
import com.fourstars.FourStars.domain.response.statistic.RevenueStatsDTO;
import com.fourstars.FourStars.repository.SubscriptionRepository;
import com.fourstars.FourStars.repository.projection.RevenueByPlanProjection;

@Service
public class StatisticService {
    private final SubscriptionRepository subscriptionRepository;

    public StatisticService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public RevenueStatsDTO getRevenueStatistics(LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<RevenueByPlanProjection> revenueByPlanProjections = subscriptionRepository.findRevenueByPlan(startInstant,
                endInstant);

        List<RevenueByPlanDTO> revenueByPlanDTOs = revenueByPlanProjections.stream()
                .map(p -> new RevenueByPlanDTO(p.getPlanId(), p.getPlanName(), p.getTransactionCount(),
                        p.getTotalRevenue()))
                .collect(Collectors.toList());

        BigDecimal totalRevenue = revenueByPlanDTOs.stream()
                .map(RevenueByPlanDTO::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTransactions = revenueByPlanDTOs.stream()
                .mapToLong(RevenueByPlanDTO::getTransactionCount)
                .sum();

        RevenueStatsDTO statsDTO = new RevenueStatsDTO();
        statsDTO.setStartDate(startDate.toString());
        statsDTO.setEndDate(endDate.toString());
        statsDTO.setTotalRevenue(totalRevenue);
        statsDTO.setTotalTransactions(totalTransactions);
        statsDTO.setRevenueByPlan(revenueByPlanDTOs);

        return statsDTO;
    }
}