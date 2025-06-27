package app.bola.taskforge.notification.service;

import app.bola.taskforge.domain.entity.NotificationPreference;
import app.bola.taskforge.event.TaskEvent;
import app.bola.taskforge.notification.model.NotificationCandidate;
import app.bola.taskforge.notification.model.NotificationPriority;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@AllArgsConstructor
public class NotificationOrchestrator {

	final DebounceEngine debounceEngine;
	final PreferenceManager preferenceManager;
	
	@EventListener
	public void processTaskEvent(TaskEvent event) {
		
		try {
			NotificationPreference preference = preferenceManager.getPreference(event.getUserId());
			if (preference != null && preference.isAllowNotification()) {
				NotificationCandidate candidate = createNotificationCandidate(event);
				debounceEngine.submit(candidate, preference);
			}
		} catch (Exception exception) {
			log.error("Failed to process task event: {}", event.getEventId(), exception);
		}
	}
	
	private NotificationCandidate createNotificationCandidate(TaskEvent event) {
		return NotificationCandidate.builder()
				       .notificationType(event.getEventType())
				       .debounceKey(generateDebounceKey(event.getUserId(), event.getTaskId(), event.getEventType().name()))
				       .userId(event.getUserId())
                       .sourceEntityId(event.getTaskId())
				       .sourceEntityType(event.getSourceEntityType())
				       .priority(NotificationPriority.HIGH)
				       .createdAt(Instant.now())
				       .build();
	}
	
	private String generateDebounceKey(String userId, String taskId, String eventType) {
		return String.format("%s:%s:%s", userId, taskId, eventType);
	}
}
