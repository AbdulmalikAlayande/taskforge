package app.bola.taskforge.notification.config;

import app.bola.taskforge.notification.interceptor.TenantChannelInterceptor;
import app.bola.taskforge.notification.interceptor.WebsocketLoggingHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	@Value("${spring.rabbitmq.client-login}")
	private String clientLogin;
	@Value("${spring.rabbitmq.client-passcode}")
	private String clientPasscode;
	
	final TenantChannelInterceptor tenantChannelInterceptor;
	final WebsocketLoggingHandshakeInterceptor loggingHandshakeInterceptor;
	
	public WebSocketConfig(TenantChannelInterceptor tenantChannelInterceptor, WebsocketLoggingHandshakeInterceptor loggingHandshakeInterceptor) {
		this.tenantChannelInterceptor = tenantChannelInterceptor;
		this.loggingHandshakeInterceptor = loggingHandshakeInterceptor;
	}
	
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(tenantChannelInterceptor);
	}
	
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/taskforge")
				.enableStompBrokerRelay("/topic", "/queue")
				.setRelayHost("rabbitmq-host")
				.setRelayPort(61613)
				.setClientLogin(clientLogin)
				.setClientPasscode(clientPasscode);
	}
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/taskforge-ws")
				.addInterceptors(loggingHandshakeInterceptor)
				.setAllowedOrigins("https://task-forge-theta.vercel.app","https://taskforge.tech", "http://localhost:3000")
				.withSockJS();
	}
}
