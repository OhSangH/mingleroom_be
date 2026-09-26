package com.mingleroom.common.config;

import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
import com.mingleroom.security.ws.StompAuthenticationInterceptor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig  implements WebSocketMessageBrokerConfigurer {
    private final StompAuthenticationInterceptor authenticationInterceptor;
    private final com.mingleroom.security.ws.RoomOutboundGuard outboundGuard;
    @Override public void configureClientOutboundChannel(ChannelRegistration registration){registration.interceptors(outboundGuard);}

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authenticationInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws-stomp")
                .setAllowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/pub");
        registry.enableSimpleBroker("/sub");
    }
}
