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

    public static final String NOTIFICATION_EXCHANGE = "notification_exchange";
    public static final String NOTIFICATION_QUEUE = "q.notification";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.#";

    public static final String QUIZ_SCORING_EXCHANGE = "quiz_scoring_exchange";
    public static final String QUIZ_SCORING_QUEUE = "q.quiz_scoring";
    public static final String QUIZ_SCORING_ROUTING_KEY = "quiz.submission";

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
}
