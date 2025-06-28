package app.bola.taskforge.notification.service;

import app.bola.taskforge.domain.entity.NotificationPreference;
import app.bola.taskforge.event.TaskEvent;
import app.bola.taskforge.notification.model.NotificationCandidate;
import app.bola.taskforge.notification.model.NotificationPriority;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class NotificationService {

	final DebounceEngine debounceEngine;
	final PreferenceManager preferenceManager;
	
	public void processTaskEvent(TaskEvent event) {
		
		try {
			List<NotificationPreference> preferences = event.getUserIdList().stream()
				                                            .map(preferenceManager::getPreference)
				                                            .filter(NotificationPreference::isAllowNotification)
				                                            .toList();
			
			if (preferences.isEmpty()) {
				log.info("No notification preferences found for users: {}", event.getUserIdList());
				return;
			}
			
			Map<NotificationCandidate, NotificationPreference> preferenceMap =  preferences.stream().map( preference -> {
				NotificationCandidate candidate = createNotificationCandidate(event, preference.getMember().getPublicId());
				return Map.entry(candidate, preference);
			}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			debounceEngine.submit(preferenceMap);
			
		} catch (Exception exception) {
			log.error("Failed to process task event: {}", event, exception);
		}
	}
	
	private NotificationCandidate createNotificationCandidate(TaskEvent event, String userId) {
		return NotificationCandidate.builder()
				       .debounceKey(generateDebounceKey(userId, event.getTaskId(), event.getEventType().name()))
				       .userId(userId)
                       .sourceEntityId(event.getTaskId())
				       .sourceEntityType(event.getSourceEntityType())
				       .eventData(generateTaskEventData(event))
				       .priority(NotificationPriority.HIGH)
				       .build();
	}
	
	private Map<String, Object> generateTaskEventData(TaskEvent event) {
		return null;
	}
	
	private String generateDebounceKey(String userId, String taskId, String eventType) {
		return String.format("%s:%s:%s", userId, taskId, eventType);
	}
}
