package com.fourstars.FourStars.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Subscription;

@Repository
public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {
    boolean existsByPlanIdAndActiveTrue(Long planId);
}