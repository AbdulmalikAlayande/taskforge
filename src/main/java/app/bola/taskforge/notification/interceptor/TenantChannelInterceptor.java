package app.bola.taskforge.notification.interceptor;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TenantChannelInterceptor implements ChannelInterceptor {
	
	private final Logger logger = LoggerFactory.getLogger(TenantChannelInterceptor.class);
	
	@Override
	public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
		String tenantId = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("tenantId");
		
		if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
			String destination = Objects.requireNonNull(headerAccessor.getDestination());
			
			if (tenantId.isEmpty()) {
				throw new IllegalArgumentException("Missing tenantId attribute");
			}
			headerAccessor.getSessionAttributes().put("tenantId", tenantId);
			
			if (!destination.startsWith("/topic/" + tenantId)) {
				throw new IllegalArgumentException("Unauthorized subscription: " + destination);
			}
		}
		
		if (StompCommand.SEND.equals(headerAccessor.getCommand())) {
			logger.info("SEND command to destination: {}", headerAccessor.getDestination());
		}
		
		return ChannelInterceptor.super.preSend(message, channel);
	}
	
	@Override
	public void postSend(@NonNull Message<?> message, @NonNull MessageChannel channel, boolean sent) {
		ChannelInterceptor.super.postSend(message, channel, sent);
	}
	
	@Override
	public boolean preReceive(@NonNull MessageChannel channel) {
		return ChannelInterceptor.super.preReceive(channel);
	}
	
	@Override
	public Message<?> postReceive(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		return ChannelInterceptor.super.postReceive(message, channel);
	}
}
                             