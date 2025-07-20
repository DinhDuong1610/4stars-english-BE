package com.fourstars.FourStars.messaging;

import com.fourstars.FourStars.config.RabbitMQConfig;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.messaging.dto.notification.NewLikeMessage;
import com.fourstars.FourStars.messaging.dto.notification.NewReplyMessage;
import com.fourstars.FourStars.messaging.dto.notification.ReviewReminderMessage;
import com.fourstars.FourStars.messaging.dto.quiz.QuizResultMessage;
import com.fourstars.FourStars.service.NotificationService;
import com.fourstars.FourStars.service.UserService;
import com.fourstars.FourStars.util.constant.NotificationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
public class NotificationConsumer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationConsumer(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @RabbitHandler
    @Transactional
    public void handleNewReply(NewReplyMessage message) {
        logger.info("Received new_reply message for recipient ID: {}, actor ID: {}", message.getRecipientId(),
                message.getActorId());
        try {
            User recipient = userService.getUserEntityById(message.getRecipientId());
            User actor = userService.getUserEntityById(message.getActorId());

            String notifMessage = actor.getName() + " đã trả lời bình luận của bạn.";
            String link = "/posts/" + message.getPostId();
            logger.debug("Creating NEW_REPLY notification for user '{}'", recipient.getEmail());

            notificationService.createNotification(recipient, actor, NotificationType.NEW_REPLY, notifMessage, link);
            logger.info("Successfully processed new_reply message for recipient ID: {}", message.getRecipientId());

        } catch (Exception e) {
            logger.error("Error processing new_reply message. Payload: " + message.toString(), e);
        }
    }

    @RabbitHandler
    @Transactional
    public void handleNewLike(NewLikeMessage message) {
        logger.info("Received new_like message for recipient ID: {}, actor ID: {}", message.getRecipientId(),
                message.getActorId());
        try {
            User recipient = userService.getUserEntityById(message.getRecipientId());
            User actor = userService.getUserEntityById(message.getActorId());

            String notifMessage = actor.getName() + " đã thích bài viết của bạn.";
            String link = "/posts/" + message.getPostId();

            logger.debug("Creating NEW_LIKE_ON_POST notification for user '{}'", recipient.getEmail());
            notificationService.createNotification(recipient, actor, NotificationType.NEW_LIKE_ON_POST, notifMessage,
                    link);
            logger.info("Successfully processed new_like message for recipient ID: {}", message.getRecipientId());

        } catch (Exception e) {
            logger.error("Error processing new_like message. Payload: " + message.toString(), e);
        }
    }

    @RabbitHandler
    @Transactional
    public void handleReviewReminder(ReviewReminderMessage message) {
        logger.info("Received review_reminder message for user ID: {}", message.getRecipientId());
        try {
            User recipient = userService.getUserEntityById(message.getRecipientId());
            if (recipient == null) {
                logger.warn("Cannot find user with ID {} to send reminder.", message.getRecipientId());
                return;
            }

            String notifMessage = "Bạn có " + message.getReviewCount()
                    + " từ vựng cần ôn tập hôm nay. Vào học ngay thôi!";
            String link = "/";
            logger.debug("Creating REVIEW_REMINDER notification for user '{}'", recipient.getEmail());

            notificationService.createNotification(recipient, null, NotificationType.REVIEW_REMINDER, notifMessage,
                    link);
            logger.info("Successfully processed review_reminder message for user ID: {}", message.getRecipientId());

        } catch (Exception e) {
            logger.error("Error processing review_reminder message. Payload: " + message.toString(), e);
        }
    }

    @RabbitHandler
    @Transactional
    public void handleQuizResult(QuizResultMessage message) {
        logger.info("Received quiz_result message for user ID: {}, attempt ID: {}", message.getRecipientId(),
                message.getAttemptId());
        try {
            User recipient = userService.getUserEntityById(message.getRecipientId());

            String notifMessage = "Bài quiz '" + message.getQuizTitle() + "' của bạn đã có kết quả. Bạn đạt được "
                    + message.getScore() + " điểm.";
            String link = "/quiz/results/" + message.getAttemptId();
            logger.debug("Creating quiz_result notification for user '{}'", recipient.getEmail());

            notificationService.createNotification(recipient, null, NotificationType.NEW_CONTENT, notifMessage, link);
            logger.info("Successfully processed quiz_result message for user ID: {}", message.getRecipientId());

        } catch (Exception e) {
            logger.error("Error processing quiz_result message. Payload: " + message.toString(), e);
        }
    }
}