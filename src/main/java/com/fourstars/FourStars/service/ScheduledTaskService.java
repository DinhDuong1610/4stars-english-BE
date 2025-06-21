package com.fourstars.FourStars.service;

import com.fourstars.FourStars.config.RabbitMQConfig;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.messaging.dto.ReviewReminderMessage;
import com.fourstars.FourStars.repository.UserVocabularyRepository;
import com.fourstars.FourStars.util.constant.NotificationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final UserVocabularyRepository userVocabularyRepository;
    private final RabbitTemplate rabbitTemplate;

    public ScheduledTaskService(UserVocabularyRepository userVocabularyRepository,
            RabbitTemplate rabbitTemplate) {
        this.userVocabularyRepository = userVocabularyRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Tác vụ này sẽ tự động chạy vào 8 giờ sáng mỗi ngày (theo giờ Việt Nam).
     */
    @Scheduled(cron = "${myapp.scheduling.reminders.cron}", zone = "Asia/Ho_Chi_Minh")
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
                ReviewReminderMessage message = new ReviewReminderMessage(
                        user.getId(),
                        user.getName(),
                        reviewCount);

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.NOTIFICATION_EXCHANGE,
                        "notification.reminder.review",
                        message);
                logger.info("Sent review reminder message for user ID: {}", user.getId());
            }
        }
        logger.info("Finished sending {} reminders.", usersToNotify.size());
    }

}
