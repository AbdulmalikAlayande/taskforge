package app.bola.taskforge.event;

import java.lang.StringBuilder;
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
		
		return new StringBuilder().append("[")
								.append("initiatorId: "+initiatorId+", ")
								.append("userIdList: "+userIdList+", ")
								.append("userEmailList: "+userEmailList+", ")
								.append("organizationId: "+organizationId+", ")
								.append("dateTimeStamp: "+dateTimeStamp)
								.append("]")
								.toString();
								
	}
	
}
