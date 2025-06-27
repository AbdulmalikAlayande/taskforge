package app.bola.taskforge.notification.config;

import app.bola.taskforge.notification.interceptor.WebsocketLoggingHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	final WebsocketLoggingHandshakeInterceptor loggingHandshakeInterceptor;
	
	public WebSocketConfig(WebsocketLoggingHandshakeInterceptor loggingHandshakeInterceptor) {
		this.loggingHandshakeInterceptor = loggingHandshakeInterceptor;
	}
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/taskforge")
				.enableSimpleBroker("/topic");
	}
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/taskforge")
				.addInterceptors(loggingHandshakeInterceptor)
				.setAllowedOrigins("https://taskforge.tech", "http://localhost:3000");
		
		registry.addEndpoint("/taskforge")
				.setAllowedOrigins("*")
				.addInterceptors(loggingHandshakeInterceptor)
				.withSockJS();
	}
}
