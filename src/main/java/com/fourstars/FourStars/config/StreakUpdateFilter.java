package com.fourstars.FourStars.config;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.messaging.dto.user.UserActivityMessage;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.service.StreakService;
import com.fourstars.FourStars.util.SecurityUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class StreakUpdateFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(StreakUpdateFilter.class);

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    public StreakUpdateFilter(UserRepository userRepository, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Optional<String> userEmailOpt = SecurityUtil.getCurrentUserLogin();
            if (userEmailOpt.isPresent()) {
                userRepository.findByEmail(userEmailOpt.get()).ifPresent(user -> {
                    UserActivityMessage message = new UserActivityMessage(user.getId());
                    rabbitTemplate.convertAndSend("user_activity_exchange", "user.activity.update", message);
                });
            }
        } catch (Exception e) {
            logger.error("Could not send user activity message", e);
        }

        filterChain.doFilter(request, response);
    }
}