package app.bola.taskforge.notification.channel;

import app.bola.taskforge.notification.model.DeliveryResult;
import app.bola.taskforge.notification.model.NotificationBundle;

import java.util.concurrent.CompletableFuture;

public interface ChannelHandler {
	
	default CompletableFuture<DeliveryResult> deliverAsync(NotificationBundle bundle) {
		return CompletableFuture.supplyAsync(() -> deliver(bundle));
	}
	
	default DeliveryResult deliver(NotificationBundle bundle) {
		throw new UnsupportedOperationException("This method should be overridden by subclasses");
	}
}
