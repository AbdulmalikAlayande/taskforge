package app.bola.taskforge.notification.service;

import app.bola.taskforge.domain.entity.NotificationPreference;
import app.bola.taskforge.notification.channel.ChannelHandler;
import app.bola.taskforge.notification.model.DeliveryResult;
import app.bola.taskforge.notification.model.NotificationBundle;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@AllArgsConstructor
public class ChannelRouter {
	
	private final List<ChannelHandler> channelHandlers;
	
	public void route(NotificationBundle bundle, NotificationPreference preference) {
		//We need to do something with the preference here,
		// We need to consider quiet hours, when the current time is within the user's quiet hours window
		// We should not send the notification, but rather find a way to store it for later
		// So when it is time to send the notification (i.e. the current time is in the window of the user's quiet hours)
		// We can then send the notification, so we will like have a place that we will be storing things like this
		// And then they will execute automatically when the time is right, So we will be configuring it that
		// Each of the notifications to sent later will execute at its own time.
		route(bundle);
	}
	
	public void route(NotificationBundle bundle) {
		
		List<CompletableFuture<DeliveryResult>> futures = channelHandlers.stream()
			.filter(handler -> handler.canHandle(bundle))
			.map(handler -> handler.deliverAsync(bundle))
			.toList();
		
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.thenRun(() -> futures.forEach(future -> {
					try {
						DeliveryResult result = future.get();
						// statusService.recordDelivery(result);
					} catch (Exception e) {
						log.error("Failed to get delivery result", e);
					}
				}));
	}
}
