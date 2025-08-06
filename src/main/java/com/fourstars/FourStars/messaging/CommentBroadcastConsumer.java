package com.fourstars.FourStars.messaging;

import com.fourstars.FourStars.config.RabbitMQConfig;
import com.fourstars.FourStars.domain.Comment;
import com.fourstars.FourStars.domain.response.comment.CommentResponseDTO;
import com.fourstars.FourStars.repository.CommentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class CommentBroadcastConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CommentBroadcastConsumer.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final CommentRepository commentRepository;

    public CommentBroadcastConsumer(SimpMessagingTemplate messagingTemplate, CommentRepository commentRepository) {
        this.messagingTemplate = messagingTemplate;
        this.commentRepository = commentRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.POST_NEW_COMMENT_QUEUE)
    public void handleNewComment(CommentResponseDTO commentDTO) {
        Comment comment = commentRepository.findById(commentDTO.getId()).orElse(null);
        String destination = "/topic/posts/" + comment.getPost().getId() + "/comments";

        logger.info("Broadcasting new comment ID {} to destination '{}'", commentDTO.getId(), destination);
        messagingTemplate.convertAndSend(destination, commentDTO);
    }
}
