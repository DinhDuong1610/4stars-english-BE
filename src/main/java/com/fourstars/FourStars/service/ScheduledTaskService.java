package com.fourstars.FourStars.service;

import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.repository.UserVocabularyRepository;
import com.fourstars.FourStars.util.constant.NotificationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final UserVocabularyRepository userVocabularyRepository;
    private final NotificationService notificationService;

    public ScheduledTaskService(UserVocabularyRepository userVocabularyRepository,
            NotificationService notificationService) {
        this.userVocabularyRepository = userVocabularyRepository;
        this.notificationService = notificationService;
    }

    /**
     * Tác vụ này sẽ tự động chạy vào 8 giờ sáng mỗi ngày (theo giờ Việt Nam).
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void sendReviewReminders() {
        logger.info("Running scheduled task: Sending review reminders...");

        // 1. Tìm tất cả người dùng có từ vựng cần ôn tập
        List<User> usersToNotify = userVocabularyRepository.findUsersWithPendingReviews(Instant.now());

        // 2. Lặp qua từng người dùng và gửi thông báo tổng hợp
        for (User user : usersToNotify) {
            // Đếm chính xác số từ cần ôn tập của user này
            long reviewCount = user.getUserVocabularies().stream()
                    .filter(uv -> uv.getNextReviewAt() != null && uv.getNextReviewAt().isBefore(Instant.now()))
                    .count();

            if (reviewCount > 0) {
                String message = "Bạn có " + reviewCount + " từ vựng cần ôn tập hôm nay. Vào học ngay thôi!";
                String link = "/review"; // Link đến trang ôn tập trên frontend

                // 3. Gửi thông báo (actor là null vì đây là thông báo hệ thống)
                notificationService.createNotification(user, null, NotificationType.REVIEW_REMINDER, message, link);
                logger.info("Sent review reminder to user ID: {}", user.getId());
            }
        }
        logger.info("Finished sending {} reminders.", usersToNotify.size());
    }
}
