package app.bola.taskforge.notification.model;

import lombok.*;

import java.time.Instant;
import java.time.LocalTime;
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
	private QuietHours quietHours;
	private List<String> sourceEntityIds;
	private List<String> sourceEntityTypes;
	
	@NoArgsConstructor
	@AllArgsConstructor
	public static class QuietHours {
		private LocalTime start;
		private LocalTime end;
	}
	
}
