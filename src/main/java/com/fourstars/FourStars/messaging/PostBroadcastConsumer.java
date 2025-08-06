package com.fourstars.FourStars.messaging;

import com.fourstars.FourStars.config.RabbitMQConfig;
import com.fourstars.FourStars.messaging.dto.post.PostLikeUpdateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostBroadcastConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PostBroadcastConsumer.class);
    private final SimpMessagingTemplate messagingTemplate;

    public PostBroadcastConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.POST_LIKE_UPDATE_QUEUE)
    public void handlePostLikeUpdate(PostLikeUpdateMessage message) {
        String destination = "/topic/posts/" + message.getPostId() + "/likes";
        logger.info("Broadcasting like update for post ID {} to destination '{}'. New count: {}",
                message.getPostId(), destination, message.getTotalLikes());

        messagingTemplate.convertAndSend(destination, message);
    }
}
