package com.fourstars.FourStars.domain.response.dashboard;

import java.util.Map;

import com.fourstars.FourStars.domain.response.badge.BadgeResponseDTO;
import com.fourstars.FourStars.domain.response.plan.PlanResponseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardResponseDTO {
    private Integer userPoints;
    private long wordsToReviewCount;
    private int totalVocabulary;
    private Map<Integer, Integer> vocabularyLevelCounts;
    private int totalQuizzesCompleted;
    private Double averageQuizScore;
    private int currentStreak;
    private BadgeResponseDTO badges;
    private PlanResponseDTO currentPlan;
    private String subscriptionExpiryDate;

}
