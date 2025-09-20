package app.bola.taskforge.notification.service;

import app.bola.taskforge.domain.entity.NotificationPreference;
import app.bola.taskforge.event.ProjectEvent;
import app.bola.taskforge.event.TaskEvent;
import app.bola.taskforge.notification.model.NotificationCandidate;
import app.bola.taskforge.notification.model.NotificationPriority;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class NotificationService {

    final DebounceEngine debounceEngine;
    final PreferenceManager preferenceManager;

    private final Map<Class<?>, Function<Object, Map<String, Object>>> eventDataGenerators = new HashMap<>();

    {
        eventDataGenerators.put(TaskEvent.class, e -> generateTaskEventData((TaskEvent) e));
        eventDataGenerators.put(ProjectEvent.class, e -> generateProjectEventData((ProjectEvent) e));
        // more event types...
    }

    private Map<String, Object> generateTaskEventData(TaskEvent event) {
	    // TODO: To implement the actual logic
	    return Map.of("taskId", event.getTaskId(), "eventType", event.getEventType().name());
    }
	private Map<String, Object> generateProjectEventData(ProjectEvent event) {
		// TODO: To implement the actual logic
		return Map.of("projectId", event.getProjectId(), "eventType", event.getEventType().name());
	}
	
    public <T> void processEvent(T event) {
        try {
            List<NotificationPreference> preferences = getUserNotificationPreferences(event);
            if (preferences == null) return;
            Map<NotificationCandidate, NotificationPreference> preferenceMap = mapPreferenceToNotificationCandidate(event, preferences);
            debounceEngine.submit(preferenceMap);
        } catch (Exception exception) {
            log.error("Failed to process event: {}", event, exception);
        }
    }
	
    private <T> List<NotificationPreference> getUserNotificationPreferences(T event) {
        List<String> userIdList = extractUserIdList(event);
        List<NotificationPreference> preferences = new ArrayList<>();
        for (String userId : userIdList) {
            NotificationPreference preference = preferenceManager.getPreference(userId);
            if (preference != null && preference.isAllowNotification())
                preferences.add(preference);
        }
        if (preferences.isEmpty()) {
            log.info("No notification preferences found for users: {}", userIdList);
            return null;
        }
        return preferences;
    }
	
    private <T> List<String> extractUserIdList(T event) {
        if (event instanceof TaskEvent) {
            return ((TaskEvent) event).getUserIdList();
        } else if (event instanceof ProjectEvent) {
            return ((ProjectEvent) event).getUserIdList();
        }
        return new ArrayList<>();
    }
	
    private <T> Map<NotificationCandidate, NotificationPreference> mapPreferenceToNotificationCandidate(T event, List<NotificationPreference> preferences) {
        return preferences.stream().map(preference -> {
            NotificationCandidate candidate = createNotificationCandidate(event, preference.getMember().getPublicId());
            return Map.entry(candidate, preference);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
	
    private <T> NotificationCandidate createNotificationCandidate(T event, String userId) {
        Map<String, Object> eventData = generateEventData(event);
        String sourceEntityId = extractSourceEntityId(event);
        String sourceEntityType = extractSourceEntityType(event);
        String eventType = extractEventType(event);
        return NotificationCandidate.builder()
                .debounceKey(generateDebounceKey(userId, sourceEntityId, eventType))
                .userId(userId)
                .sourceEntityId(sourceEntityId)
                .sourceEntityType(sourceEntityType)
                .eventData(eventData)
                .priority(NotificationPriority.HIGH)
                .build();
    }
	
    private <T> Map<String, Object> generateEventData(T event) {
        Function<Object, Map<String, Object>> generator = eventDataGenerators.get(event.getClass());
        if (generator != null) {
            return generator.apply(event);
        }
        return null;
    }

    private <T> String extractSourceEntityId(T event) {
        if (event instanceof TaskEvent) {
            return ((TaskEvent) event).getTaskId();
        } else if (event instanceof ProjectEvent) {
            return ((ProjectEvent) event).getProjectId();
        }
        return "";
    }
    private <T> String extractSourceEntityType(T event) {
        if (event instanceof TaskEvent) {
            return ((TaskEvent) event).getSourceEntityType();
        } else if (event instanceof ProjectEvent) {
            return ((ProjectEvent) event).getSourceEntityType();
        }
        return "";
    }
    private <T> String extractEventType(T event) {
        if (event instanceof TaskEvent) {
            return ((TaskEvent) event).getEventType().name();
        } else if (event instanceof ProjectEvent) {
            return ((ProjectEvent) event).getEventType().name();
        }
        return "";
    }

    private String generateDebounceKey(String userId, String taskId, String eventType) {
        return String.format("%s:%s:%s", userId, taskId, eventType);
    }
}
