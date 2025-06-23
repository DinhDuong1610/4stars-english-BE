package com.fourstars.FourStars.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.service.StreakService;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StreakService streakService;

    @Test
    void updateUserStreak_shouldSetStreakToOne_forFirstTimeActivity() {
        User user = new User();
        user.setStreakCount(0);
        user.setLastActivityDate(null);

        streakService.updateUserStreak(user);

        assertEquals(1, user.getStreakCount());
        assertEquals(LocalDate.now(), user.getLastActivityDate());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUserStreak_shouldContinueStreak_whenLastActivityIsYesterday() {
        User user = new User();
        user.setStreakCount(5);
        user.setLastActivityDate(LocalDate.now().minusDays(1));

        streakService.updateUserStreak(user);

        assertEquals(6, user.getStreakCount());
        assertEquals(LocalDate.now(), user.getLastActivityDate());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUserStreak_shouldResetStreak_whenLastActivityIsBeforeYesterday() {
        User user = new User();
        user.setStreakCount(10);
        user.setLastActivityDate(LocalDate.now().minusDays(2));

        streakService.updateUserStreak(user);

        assertEquals(1, user.getStreakCount());
        assertEquals(LocalDate.now(), user.getLastActivityDate());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUserStreak_shouldNotChangeStreak_whenActiveOnSameDay() {
        User user = new User();
        user.setStreakCount(3);
        user.setLastActivityDate(LocalDate.now());

        streakService.updateUserStreak(user);

        assertEquals(3, user.getStreakCount());
        assertEquals(LocalDate.now(), user.getLastActivityDate());
        verify(userRepository, never()).save(user);
    }
}
