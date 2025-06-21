package com.fourstars.FourStars.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    public WebSocketAuthInterceptor(JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Chỉ kiểm tra khi client gửi lệnh CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Lấy token từ header "Authorization"
            List<String> authorization = accessor.getNativeHeader("Authorization");
            if (authorization == null || authorization.isEmpty()) {
                return message;
            }

            String token = authorization.get(0);
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) jwtAuthenticationConverter
                            .convert(jwt);
                    accessor.setUser(authentication);
                } catch (Exception e) {
                    System.err.println("Invalid token for WebSocket connection: " + e.getMessage());
                }
            }
        }
        return message;
    }
}
