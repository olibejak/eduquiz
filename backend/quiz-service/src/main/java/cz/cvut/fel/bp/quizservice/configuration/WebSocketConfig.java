package cz.cvut.fel.bp.quizservice.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the STOMP message broker for quiz WebSocket communication.
     * Enables the simple broker for `/topic` destinations and sets `/app` as the
     * application destination prefix.
     * @param config the message broker registry used to register broker settings
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * WebSocket endpoint registration with SockJS fallback and CORS configuration.
     * @param registry the registry to which the endpoint is added
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-quiz").setAllowedOriginPatterns("*").withSockJS();
    }
}
