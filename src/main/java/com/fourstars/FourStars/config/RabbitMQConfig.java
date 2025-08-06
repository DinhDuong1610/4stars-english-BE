package com.fourstars.FourStars.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String USER_ACTIVITY_EXCHANGE = "user_activity_exchange";
    public static final String USER_ACTIVITY_QUEUE = "q.user_activity";
    public static final String USER_ACTIVITY_ROUTING_KEY = "user.activity.#";

    public static final String NOTIFICATION_EXCHANGE = "notification_exchange";
    public static final String NOTIFICATION_QUEUE = "q.notification";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.#";

    public static final String QUIZ_SCORING_EXCHANGE = "quiz_scoring_exchange";
    public static final String QUIZ_SCORING_QUEUE = "q.quiz_scoring";
    public static final String QUIZ_SCORING_ROUTING_KEY = "quiz.submission";

    public static final String VOCABULARY_EVENT_EXCHANGE = "vocabulary_event_exchange";
    public static final String VOCABULARY_CREATED_QUEUE = "q.vocabulary.created";
    public static final String VOCABULARY_CREATED_ROUTING_KEY = "vocabulary.created";

    public static final String POST_BROADCAST_EXCHANGE = "post_broadcast_exchange";
    public static final String POST_LIKE_UPDATE_QUEUE = "q.post.like_update";
    public static final String POST_LIKE_UPDATE_ROUTING_KEY = "post.like.update";
    public static final String POST_NEW_COMMENT_QUEUE = "q.post.new_comment";
    public static final String POST_NEW_COMMENT_ROUTING_KEY = "post.comment.new";

    @Bean
    public TopicExchange userActivityExchange() {
        return new TopicExchange(USER_ACTIVITY_EXCHANGE);
    }

    @Bean
    public Queue userActivityQueue() {
        return new Queue(USER_ACTIVITY_QUEUE, true);
    }

    @Bean
    public Binding userActivityBinding() {
        return BindingBuilder.bind(userActivityQueue()).to(userActivityExchange()).with(USER_ACTIVITY_ROUTING_KEY);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public TopicExchange quizScoringExchange() {
        return new TopicExchange(QUIZ_SCORING_EXCHANGE);
    }

    @Bean
    public Queue quizScoringQueue() {
        return new Queue(QUIZ_SCORING_QUEUE, true);
    }

    @Bean
    public Binding quizScoringBinding() {
        return BindingBuilder
                .bind(quizScoringQueue())
                .to(quizScoringExchange())
                .with(QUIZ_SCORING_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange vocabularyEventExchange() {
        return new TopicExchange(VOCABULARY_EVENT_EXCHANGE);
    }

    @Bean
    public Queue vocabularyCreatedQueue() {
        return new Queue(VOCABULARY_CREATED_QUEUE, true);
    }

    @Bean
    public Binding vocabularyCreatedBinding() {
        return BindingBuilder
                .bind(vocabularyCreatedQueue())
                .to(vocabularyEventExchange())
                .with(VOCABULARY_CREATED_ROUTING_KEY);
    }

    @Bean
    public TopicExchange postBroadcastExchange() {
        return new TopicExchange(POST_BROADCAST_EXCHANGE);
    }

    @Bean
    public Queue postLikeUpdateQueue() {
        return new Queue(POST_LIKE_UPDATE_QUEUE, true);
    }

    @Bean
    public Binding postLikeUpdateBinding() {
        return BindingBuilder
                .bind(postLikeUpdateQueue())
                .to(postBroadcastExchange())
                .with(POST_LIKE_UPDATE_ROUTING_KEY);
    }

    @Bean
    public Queue postNewCommentQueue() {
        return new Queue(POST_NEW_COMMENT_QUEUE, true);
    }

    @Bean
    public Binding postNewCommentBinding() {
        return BindingBuilder
                .bind(postNewCommentQueue())
                .to(postBroadcastExchange())
                .with(POST_NEW_COMMENT_ROUTING_KEY);
    }
}
