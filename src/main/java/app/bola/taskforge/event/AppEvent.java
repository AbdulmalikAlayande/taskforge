package app.bola.taskforge.event;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AppEvent {
	
	private String eventId;
	private EventType eventType;
	private LocalDateTime timestamp;
	private String initiatorId;
	private String organizationId;
	private String metadata;
}
