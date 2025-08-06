package com.fourstars.FourStars.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Subscription;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.repository.projection.DateCountProjection;
import com.fourstars.FourStars.repository.projection.DateRevenueProjection;
import com.fourstars.FourStars.repository.projection.RevenueByPlanProjection;

@Repository
public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {
    boolean existsByPlanIdAndActiveTrue(Long planId);

    Optional<Subscription> findTopByUserOrderByEndDateDesc(User user);

    Page<Subscription> findByUserId(Long userId, Pageable pageable);

    List<Subscription> findByUserId(Long userId);

    Optional<Subscription> findByUserIdAndPlanIdAndActiveTrue(Long userId, Long planId);

    List<Subscription> findByEndDateBeforeAndActiveTrue(Instant now);

    long countByActiveTrue();

    @Query("SELECT SUM(p.price) FROM Subscription s JOIN s.plan p WHERE s.paymentStatus = 'PAID'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT SUM(p.price) FROM Subscription s JOIN s.plan p WHERE s.paymentStatus = 'PAID' AND s.createdAt >= :date")
    BigDecimal calculateRevenueAfter(Instant date);

    @Query(value = "SELECT CAST(updated_at AS DATE) as date, COUNT(id) as count FROM subscriptions WHERE payment_status = 'PAID' AND updated_at >= :startDate GROUP BY CAST(updated_at AS DATE) ORDER BY date ASC", nativeQuery = true)
    List<DateCountProjection> findPremiumUpgradeCountByDate(Instant startDate);

    @Query(value = "SELECT CAST(s.updated_at AS DATE) as date, SUM(p.price) as total FROM subscriptions s JOIN plans p ON s.plan_id = p.id WHERE s.payment_status = 'PAID' AND s.updated_at >= :startDate GROUP BY CAST(s.updated_at AS DATE) ORDER BY date ASC", nativeQuery = true)
    List<DateRevenueProjection> findRevenueByDate(Instant startDate);

    @Query("SELECT p.id as planId, p.name as planName, COUNT(s.id) as transactionCount, SUM(p.price) as totalRevenue " +
            "FROM Subscription s JOIN s.plan p " +
            "WHERE s.paymentStatus = com.fourstars.FourStars.util.constant.PaymentStatus.PAID " +
            "AND s.updatedAt >= :startDate AND s.updatedAt < :endDate " +
            "GROUP BY p.id, p.name " +
            "ORDER BY totalRevenue DESC")
    List<RevenueByPlanProjection> findRevenueByPlan(Instant startDate, Instant endDate);
}