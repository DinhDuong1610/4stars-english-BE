package com.fourstars.FourStars.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    public WebSocketAuthInterceptor(JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            logger.debug("Intercepting STOMP CONNECT command for session ID: {}", accessor.getSessionId());

            List<String> authorization = accessor.getNativeHeader("Authorization");
            if (authorization == null || authorization.isEmpty()) {
                logger.warn("WebSocket CONNECT request for session ID {} without 'Authorization' header.",
                        accessor.getSessionId());

                return message;
            }

            String token = authorization.get(0);
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                logger.debug("Found Bearer token for WebSocket session ID: {}. Attempting to validate.",
                        accessor.getSessionId());

                try {
                    Jwt jwt = jwtDecoder.decode(token);

                    Authentication authentication = jwtAuthenticationConverter.convert(jwt);

                    accessor.setUser(authentication);
                    logger.info("Successfully authenticated user '{}' for WebSocket session ID: {}",
                            authentication.getName(), accessor.getSessionId());

                } catch (Exception e) {
                    logger.error("Invalid JWT token for WebSocket connection. Session ID: {}. Error: {}",
                            accessor.getSessionId(), e.getMessage());
                }
            }
        }
        return message;
    }
}
