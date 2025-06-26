package app.bola.taskforge.notification.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class NotificationBundle {
	
	private String title;
	private String userId;
	private String emailTo;
	private String channel;
	private String message;
	private String htmlMessage;
	private Instant scheduledAt;
	private Map<String, Object> payload;
	private Set<ChannelType> channels;
	private List<String> sourceEntityIds;
	
	
}
