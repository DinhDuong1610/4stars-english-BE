package com.fourstars.FourStars.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Quiz;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.UserQuizAttempt;
import com.fourstars.FourStars.repository.projection.LeaderboardProjection;
import com.fourstars.FourStars.util.constant.QuizStatus;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Long> {
        Optional<UserQuizAttempt> findByUserAndQuizAndStatus(User user, Quiz quiz, QuizStatus status);

        Optional<UserQuizAttempt> findByIdAndUserId(long id, long userId);

        int countByUser(User user);

        @Query("SELECT AVG(ua.score) FROM UserQuizAttempt ua WHERE ua.user = :user")
        Double calculateAverageScoreByUser(User user);

        @Query("SELECT u.id as userId, u.name as name, u.email as email, SUM(a.score) as weeklyPoints " +
                        "FROM UserQuizAttempt a JOIN a.user u " +
                        "WHERE a.status = com.fourstars.FourStars.util.constant.QuizStatus.COMPLETED " +
                        "AND a.completedAt >= :startDate AND a.completedAt < :endDate " +
                        "AND u.role.name IN ('USER', 'PREMIUM') " +
                        "AND (:badgeId IS NULL OR u.badge.id = :badgeId) " +
                        "GROUP BY u.id, u.name, u.email " +
                        "ORDER BY weeklyPoints DESC, MAX(a.completedAt) ASC")
        Page<LeaderboardProjection> findWeeklyLeaderboard(
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        @Param("badgeId") Long badgeId,
                        Pageable pageable);
}