package com.fourstars.FourStars.service;

import com.fourstars.FourStars.config.RabbitMQConfig;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.messaging.dto.notification.ReviewReminderMessage;
import com.fourstars.FourStars.repository.UserVocabularyRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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

    @Scheduled(cron = "${myapp.scheduling.reminders.cron}", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void sendReviewReminders() {
        logger.info("==================== Running scheduled task: Sending review reminders... ====================");

        List<User> usersToNotify = userVocabularyRepository.findUsersWithPendingReviews(Instant.now());
        if (usersToNotify.isEmpty()) {
            logger.info("No users with pending reviews found. Task finished.");
            return;
        }

        logger.info("Found {} user(s) with pending reviews to notify.", usersToNotify.size());
        logger.debug("User IDs to be notified: {}",
                usersToNotify.stream().map(User::getId).collect(Collectors.toList()));

        for (User user : usersToNotify) {
            logger.debug("Processing user ID: {}. Counting overdue vocabularies...", user.getId());

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
                logger.info("Sent review reminder message for user ID: {}, Name: '{}', Count: {}", user.getId(),
                        user.getName(), reviewCount);
            } else {
                logger.warn("User ID {} was found by query but has 0 reviewable words after count. Skipping.",
                        user.getId());
            }
        }
        logger.info("Finished sending {} reminders.", usersToNotify.size());
        logger.info("Finished sending {} reminder messages. Task complete.", usersToNotify.size());
    }

}
