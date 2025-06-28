package app.bola.taskforge.notification.model;

import app.bola.taskforge.event.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCandidate {
    
    private String userId;
    private String debounceKey;
    private EventType notificationType;
    private String sourceEntityId;
    private String sourceEntityType;
    private Map<String, Object> eventData;
    private NotificationPriority priority;
}