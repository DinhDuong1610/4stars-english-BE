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
        logger.info("[START] Processing quiz scoring for attempt ID: {}. User ID: {}",
                message.getUserQuizAttemptId(), message.getUserId());
        try {
            quizService.processAndScoreQuiz(message);
            logger.info("[SUCCESS] Finished scoring quiz for attempt ID: {}", message.getUserQuizAttemptId());

        } catch (ResourceNotFoundException | BadRequestException e) {
            logger.warn("[SKIPPED] Business logic error while scoring attempt ID {}: {} - {}",
                    message.getUserQuizAttemptId(), e.getClass().getSimpleName(), e.getMessage());

        } catch (Exception e) {
            logger.error("[FAILED] Unexpected error scoring attempt ID: {}", message.getUserQuizAttemptId(), e);
        }
    }
}
