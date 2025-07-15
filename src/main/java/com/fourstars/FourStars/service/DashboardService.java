package com.fourstars.FourStars.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.response.dashboard.AdminDashboardResponseDTO;
import com.fourstars.FourStars.repository.ArticleRepository;
import com.fourstars.FourStars.repository.PostRepository;
import com.fourstars.FourStars.repository.QuizRepository;
import com.fourstars.FourStars.repository.SubscriptionRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.repository.VocabularyRepository;
import com.fourstars.FourStars.repository.projection.DateCountProjection;
import com.fourstars.FourStars.repository.projection.DateRevenueProjection;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final VocabularyRepository vocabularyRepository;
    private final QuizRepository quizRepository;
    private final ArticleRepository articleRepository;
    private final PostRepository postRepository;

    public DashboardService(UserRepository userRepository, SubscriptionRepository subscriptionRepository,
            VocabularyRepository vocabularyRepository, QuizRepository quizRepository,
            ArticleRepository articleRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.quizRepository = quizRepository;
        this.articleRepository = articleRepository;
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponseDTO getAdminDashboardStats() {
        AdminDashboardResponseDTO dto = new AdminDashboardResponseDTO();

        Instant today = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant thirtyDaysAgo = today.minus(30, ChronoUnit.DAYS);

        dto.setTotalUsers(userRepository.count());
        Instant firstDayOfMonth = YearMonth.now().atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        dto.setNewUsersThisMonth(userRepository.countByCreatedAtAfter(firstDayOfMonth));

        dto.setTotalActiveSubscriptions(subscriptionRepository.countByActiveTrue());
        dto.setTotalRevenue(
                subscriptionRepository.calculateTotalRevenue() != null ? subscriptionRepository.calculateTotalRevenue()
                        : BigDecimal.ZERO);
        dto.setRevenueThisMonth(subscriptionRepository.calculateRevenueAfter(firstDayOfMonth) != null
                ? subscriptionRepository.calculateRevenueAfter(firstDayOfMonth)
                : BigDecimal.ZERO);

        Map<String, Long> contentCount = new HashMap<>();
        contentCount.put("vocabularies", vocabularyRepository.count());
        contentCount.put("quizzes", quizRepository.count());
        contentCount.put("articles", articleRepository.count());
        contentCount.put("posts", postRepository.count());
        dto.setContentCount(contentCount);

        List<DateCountProjection> newUserStats = userRepository.findNewUserCountByDate(thirtyDaysAgo);
        dto.setNewUserRegistrationsChart(fillMissingDatesForCount(newUserStats, 30));

        List<DateCountProjection> premiumUpgradeStats = subscriptionRepository
                .findPremiumUpgradeCountByDate(thirtyDaysAgo);
        dto.setPremiumUpgradesChart(fillMissingDatesForCount(premiumUpgradeStats, 30));

        List<DateRevenueProjection> revenueStats = subscriptionRepository.findRevenueByDate(thirtyDaysAgo);
        dto.setRevenueChart(fillMissingDatesForRevenue(revenueStats, 30));

        return dto;
    }

    private List<AdminDashboardResponseDTO.ChartDataPoint> fillMissingDatesForCount(List<DateCountProjection> dbResults,
            int days) {
        Map<LocalDate, Long> resultMap = dbResults.stream()
                .collect(Collectors.toMap(DateCountProjection::getDate, DateCountProjection::getCount));

        LocalDate startDate = LocalDate.now().minusDays(days - 1);

        return IntStream.range(0, days)
                .mapToObj(startDate::plusDays)
                .map(date -> {
                    Long count = resultMap.getOrDefault(date, 0L);
                    return new AdminDashboardResponseDTO.ChartDataPoint(date.toString(), count);
                })
                .collect(Collectors.toList());
    }

    private List<AdminDashboardResponseDTO.ChartDataPoint> fillMissingDatesForRevenue(
            List<DateRevenueProjection> dbResults,
            int days) {
        Map<LocalDate, BigDecimal> resultMap = dbResults.stream()
                .collect(Collectors.toMap(DateRevenueProjection::getDate, DateRevenueProjection::getTotal));

        LocalDate startDate = LocalDate.now().minusDays(days - 1);

        return IntStream.range(0, days)
                .mapToObj(startDate::plusDays)
                .map(date -> {
                    BigDecimal total = resultMap.getOrDefault(date, BigDecimal.ZERO);
                    return new AdminDashboardResponseDTO.ChartDataPoint(date.toString(), total);
                })
                .collect(Collectors.toList());
    }

}
