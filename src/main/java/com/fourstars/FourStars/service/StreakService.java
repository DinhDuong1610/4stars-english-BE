package com.fourstars.FourStars.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.repository.UserRepository;

@Service
public class StreakService {
    private static final Logger logger = LoggerFactory.getLogger(StreakService.class);

    private final UserRepository userRepository;

    public StreakService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void updateUserStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastActivity = user.getLastActivityDate();
        logger.debug("Checking streak for user ID: {}. Last activity: {}, Today: {}", user.getId(), lastActivity,
                today);

        if (lastActivity != null && lastActivity.isEqual(today)) {
            logger.debug("User {} already active today. No streak update needed.", user.getId());
            return;
        }

        int currentStreak = (user.getStreakCount() == null) ? 0 : user.getStreakCount();

        if (lastActivity == null) {
            logger.info("First time activity for user ID: {}. Setting streak to 1.", user.getId());
            user.setStreakCount(1);
        } else {
            if (lastActivity.isEqual(today.minusDays(1))) {
                user.setStreakCount(currentStreak + 1);
                logger.info("Streak continued for user ID: {}. New streak: {}", user.getId(), currentStreak + 1);

            } else {
                user.setStreakCount(1);
                logger.warn("Streak broken for user ID: {}. Resetting streak to 1. Old streak was {}.", user.getId(),
                        currentStreak);

            }
        }

        user.setLastActivityDate(today);
        userRepository.save(user);
    }
}
