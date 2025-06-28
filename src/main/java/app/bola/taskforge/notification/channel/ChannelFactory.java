package app.bola.taskforge.notification.channel;

import app.bola.taskforge.notification.model.ChannelType;
import app.bola.taskforge.notification.template.NotificationTemplateRender;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@AllArgsConstructor
public class ChannelFactory {
	
	final SimpMessagingTemplate messagingTemplate;
	final RestTemplate restTemplate;
	final NotificationTemplateRender templateRender;
	
	
	public ChannelHandler createChannelHandler(ChannelType channelType) {
		return switch (channelType) {
			case EMAIL -> new EmailChannelHandler(restTemplate, templateRender);
			case WEBSOCKET -> new WebSocketChannelHandler(messagingTemplate);
			default -> throw new IllegalArgumentException("Unsupported channel type: " + channelType);
		};
	}
}
