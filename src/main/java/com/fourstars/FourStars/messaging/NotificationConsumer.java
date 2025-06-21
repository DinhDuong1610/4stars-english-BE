package com.fourstars.FourStars.messaging;

import com.fourstars.FourStars.config.RabbitMQConfig;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.messaging.dto.NewReplyMessage;
import com.fourstars.FourStars.service.NotificationService;
import com.fourstars.FourStars.service.UserService;
import com.fourstars.FourStars.util.constant.NotificationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationConsumer(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
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
}