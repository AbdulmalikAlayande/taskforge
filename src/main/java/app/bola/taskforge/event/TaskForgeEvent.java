package app.bola.taskforge.event;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class TaskForgeEvent extends ApplicationEvent {
	
	private String metadata;
	private String initiatorId;
	private List<String> userIdList;
	private List<String> userEmailList;
	private String organizationId;
	private LocalDateTime dateTimeStamp;
	
	public TaskForgeEvent(Object source) {
		super(source);
	}

	@Override
	public String toString() {
		
		return "[" +
				       "initiatorId: " + initiatorId + ", " +
				       "userIdList: " + userIdList + ", " +
				       "userEmailList: " + userEmailList + ", " +
				       "organizationId: " + organizationId + ", " +
				       "dateTimeStamp: " + dateTimeStamp +
				       "]";
								
	}
	
}
