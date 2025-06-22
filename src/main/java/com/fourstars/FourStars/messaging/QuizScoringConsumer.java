package com.fourstars.FourStars.messaging;

import com.fourstars.FourStars.config.RabbitMQConfig;
import com.fourstars.FourStars.messaging.dto.quiz.QuizSubmissionMessage;
import com.fourstars.FourStars.service.QuizService;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class QuizScoringConsumer {
    private static final Logger logger = LoggerFactory.getLogger(QuizScoringConsumer.class);

    private final QuizService quizService;

    public QuizScoringConsumer(QuizService quizService) {
        this.quizService = quizService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUIZ_SCORING_QUEUE)
    public void handleQuizScoring(QuizSubmissionMessage message) {
        logger.info("Received quiz submission to score from user: {}", message.getUserId());
        try {
            quizService.processAndScoreQuiz(message);
            logger.info("Successfully scored quiz for attempt: {}", message.getUserQuizAttemptId());

        } catch (ResourceNotFoundException | BadRequestException e) {
            logger.warn("Business logic error while scoring quiz for attempt {}: {}", message.getUserQuizAttemptId(),
                    e.getMessage());

        } catch (Exception e) {
            logger.error("Unexpected error scoring quiz for attempt: " + message.getUserQuizAttemptId(), e);
        }
    }
}
