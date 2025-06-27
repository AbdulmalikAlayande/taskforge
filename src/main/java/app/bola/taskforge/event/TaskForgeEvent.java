package app.bola.taskforge.event;

import java.time.LocalDateTime;
import lombok.*;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class TaskForgeEvent extends ApplicationEvent {
	
	private String eventId;
	private String userId;
	private String userEmail;
	private EventType eventType;
	private LocalDateTime dateTimeStamp;
	private String initiatorId;
	private String organizationId;
	private String metadata;
	
	public TaskForgeEvent(Object source) {
		super(source);
	}
}
