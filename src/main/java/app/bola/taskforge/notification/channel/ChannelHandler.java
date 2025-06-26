package app.bola.taskforge.notification.channel;

import app.bola.taskforge.notification.model.ChannelType;
import app.bola.taskforge.notification.model.DeliveryResult;
import app.bola.taskforge.notification.model.NotificationBundle;

import java.util.concurrent.CompletableFuture;

public interface ChannelHandler {
	
	default ChannelType getChannelType() {
		return null;
	}
	
	default boolean canHandle(NotificationBundle bundle) {
		return false;
	}
	
	default CompletableFuture<DeliveryResult> deliverAsync(NotificationBundle bundle) {
		return CompletableFuture.supplyAsync(() -> deliver(bundle));
	}
	
	default DeliveryResult deliver(NotificationBundle bundle) {
		throw new UnsupportedOperationException("This method should be overridden by subclasses");
	}
}
