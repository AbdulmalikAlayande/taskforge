package app.bola.taskforge.notification.consumer;

import app.bola.taskforge.event.TaskEvent;
import app.bola.taskforge.event.ProjectEvent;
import app.bola.taskforge.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {
	
	final NotificationService notificationService;
	
	@EventListener
	public void consumeTaskEvent(TaskEvent event) {
		notificationService.processTaskEvent(event);
	}

	@EventListener
	public void consumeProjectEvent(ProjectEvent event){
		notificationService.processProjectEvent(event);
	}
}
