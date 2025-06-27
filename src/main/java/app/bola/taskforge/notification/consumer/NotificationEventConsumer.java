package app.bola.taskforge.notification.consumer;

import app.bola.taskforge.event.TaskEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {
	
	final ApplicationEventPublisher eventPublisher;
	
	@PulsarListener(
		topics = {"${pulsar.topic.notification}"},
		subscriptionName = "${pulsar.subscription.notification}"
	)
	public void consumeTaskEvent(TaskEvent event) {
		eventPublisher.publishEvent(event);
	}
}
