package app.bola.taskforge.notification.consumer;

import app.bola.taskforge.event.AppEvent;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {
	
	@PulsarListener(
		topics = {"${pulsar.topic.notification}"},
		subscriptionName = "${pulsar.subscription.notification}"
	)
	public void consumeEvent(AppEvent event) {
		System.out.println("Event consumed: " + event);
	}
}
