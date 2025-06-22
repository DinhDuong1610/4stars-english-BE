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
        logger.info("Received new reply message: {}", message);
        try {
            User recipient = userService.getUserEntityById(message.getRecipientId());
            User actor = userService.getUserEntityById(message.getActorId());

            String notifMessage = actor.getName() + " đã trả lời bình luận của bạn.";
            String link = "/api/v1/posts/" + message.getPostId() + "#comment-" + message.getCommentId();

            notificationService.createNotification(recipient, actor, NotificationType.NEW_REPLY, notifMessage, link);

        } catch (Exception e) {
            logger.error("Error processing new reply notification message", e);
        }
    }

    @RabbitHandler
    @Transactional
    public void handleNewLike(NewLikeMessage message) {
        logger.info("Received new like message: {}", message);
        try {
            User recipient = userService.getUserEntityById(message.getRecipientId());
            User actor = userService.getUserEntityById(message.getActorId());

            String notifMessage = actor.getName() + " đã thích bài viết của bạn.";
            String link = "/api/v1/posts/" + message.getPostId();

            notificationService.createNotification(recipient, actor, NotificationType.NEW_LIKE_ON_POST, notifMessage,
                    link);

        } catch (Exception e) {
            logger.error("Error processing new like notification message", e);
        }
    }

    @RabbitHandler
    @Transactional
    public void handleReviewReminder(ReviewReminderMessage message) {
        logger.info("Received review reminder message for user: {}", message.getRecipientId());
        try {
            User recipient = userService.getUserEntityById(message.getRecipientId());
            if (recipient == null) {
                logger.warn("Cannot find user with ID {} to send reminder.", message.getRecipientId());
                return;
            }

            String notifMessage = "Bạn có " + message.getReviewCount()
                    + " từ vựng cần ôn tập hôm nay. Vào học ngay thôi!";
            String link = "/api/v1/review";

            notificationService.createNotification(recipient, null, NotificationType.REVIEW_REMINDER, notifMessage,
                    link);

        } catch (Exception e) {
            logger.error("Error processing review reminder message", e);
        }
    }

    @RabbitHandler
    @Transactional
    public void handleQuizResult(QuizResultMessage message) {
        logger.info("Received quiz result message for attempt: {}", message.getAttemptId());
        try {
            User recipient = userService.getUserEntityById(message.getRecipientId());

            String notifMessage = "Bài quiz '" + message.getQuizTitle() + "' của bạn đã có kết quả. Bạn đạt được "
                    + message.getScore() + " điểm.";
            String link = "/quiz/results/" + message.getAttemptId();

            notificationService.createNotification(recipient, null, NotificationType.NEW_CONTENT, notifMessage, link);
        } catch (Exception e) {
            logger.error("Error processing quiz result notification", e);
        }
    }
}