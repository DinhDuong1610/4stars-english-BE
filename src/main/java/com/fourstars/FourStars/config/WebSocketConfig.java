package com.fourstars.FourStars.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đây là endpoint mà client sẽ kết nối tới để bắt đầu một phiên WebSocket.
        // Ví dụ: new SockJS("http://localhost:8080/ws")
        registry.addEndpoint("/ws")
                // Cho phép các domain của frontend kết nối tới
                .setAllowedOrigins("http://localhost:3000", "http://localhost:4173", "http://localhost:5173")
                // Dùng SockJS để hỗ trợ các trình duyệt cũ không hỗ trợ WebSocket thuần.
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // "/app" là tiền tố cho các message gửi từ client đến server.
        // Ví dụ: client gửi message đến destination "/app/chat", server sẽ xử lý nó.
        registry.setApplicationDestinationPrefixes("/app");

        // "/topic" và "/queue" là tiền tố cho các destination mà server sẽ gửi message
        // đến client.
        // - "/topic": Dùng cho các message broadcast (gửi đến tất cả mọi người đăng
        // ký).
        // - "/queue": Dùng cho các message cá nhân (chỉ gửi đến một người dùng cụ thể).
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Đăng ký interceptor của chúng ta để nó được thực thi trước khi xử lý message
        registration.interceptors(webSocketAuthInterceptor);
    }
}
