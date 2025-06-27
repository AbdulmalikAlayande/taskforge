package app.bola.taskforge.notification.model;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationBundle {
	
	private String id;
	private String title;
	private String userId;
	private String emailTo;
	private String message;
	private String htmlMessage;
	private Instant scheduledAt;
	private Map<String, Object> payload;
	private Set<ChannelType> channels;
	private List<String> sourceEntityIds;
	
	
}
