package com.fourstars.FourStars.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.repository.UserRepository;

@Service
public class StreakService {

    private final UserRepository userRepository;

    public StreakService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void updateUserStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastActivity = user.getLastActivityDate();

        if (lastActivity != null && lastActivity.isEqual(today)) {
            return;
        }

        int currentStreak = (user.getStreakCount() == null) ? 0 : user.getStreakCount();

        if (lastActivity == null) {
            user.setStreakCount(1);
        } else {
            if (lastActivity.isEqual(today.minusDays(1))) {
                user.setStreakCount(currentStreak + 1);
            } else {
                user.setStreakCount(1);
            }
        }

        user.setLastActivityDate(today);
        userRepository.save(user);
    }
}
