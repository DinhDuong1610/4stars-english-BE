package com.fourstars.FourStars.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Subscription;
import com.fourstars.FourStars.domain.User;

@Repository
public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {
    boolean existsByPlanIdAndActiveTrue(Long planId);

    Optional<Subscription> findTopByUserOrderByEndDateDesc(User user);

    Page<Subscription> findByUserId(Long userId, Pageable pageable);

    List<Subscription> findByUserId(Long userId);

    Optional<Subscription> findByUserIdAndPlanIdAndActiveTrue(Long userId, Long planId);

    List<Subscription> findByEndDateBeforeAndActiveTrue(Instant now);
}