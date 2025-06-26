package app.bola.taskforge.notification.channel;

import app.bola.taskforge.notification.model.ChannelType;
import app.bola.taskforge.notification.model.DeliveryResult;
import app.bola.taskforge.notification.model.NotificationBundle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChannelHandler implements ChannelHandler{
	
	final SimpMessagingTemplate messagingTemplate;
	
	@Override
	public ChannelType getChannelType() {
		return ChannelType.WEBSOCKET;
	}
	
	@Override
	public boolean canHandle(NotificationBundle bundle) {
		return bundle.getChannels().contains(ChannelType.WEBSOCKET);
	}
	
	@Override
	public DeliveryResult deliver(NotificationBundle bundle) {
		
		try {
			String destination = "/user/" + bundle.getUserId() + "/notifications";
			messagingTemplate.convertAndSendToUser(bundle.getUserId(), destination, bundle);
			
			log.debug("WebSocket notification sent to user: {}", bundle.getUserId());
			return DeliveryResult.success(bundle.getId(), ChannelType.WEBSOCKET, destination);
			
		} catch (Exception e) {
			log.error("WebSocket delivery failed for bundle: {}", bundle.getId(), e);
			return DeliveryResult.failure(bundle.getId(), ChannelType.WEBSOCKET, e.getMessage());
		}
	}
	
	@Override
	public CompletableFuture<DeliveryResult> deliverAsync(NotificationBundle bundle) {
		return ChannelHandler.super.deliverAsync(bundle);
	}
}
