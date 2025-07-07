package com.fourstars.FourStars.messaging;

import com.fourstars.FourStars.config.RabbitMQConfig;
import com.fourstars.FourStars.messaging.dto.user.UserActivityMessage;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.service.StreakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ActivityConsumer {
    private static final Logger logger = LoggerFactory.getLogger(ActivityConsumer.class);

    private final StreakService streakService;
    private final UserRepository userRepository;

    public ActivityConsumer(StreakService streakService, UserRepository userRepository) {
        this.streakService = streakService;
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.USER_ACTIVITY_QUEUE)
    public void handleUserActivity(UserActivityMessage message) {
        logger.debug("Received user activity message for user ID: {}", message.getUserId());
        userRepository.findById(message.getUserId()).ifPresent(streakService::updateUserStreak);
    }
}
