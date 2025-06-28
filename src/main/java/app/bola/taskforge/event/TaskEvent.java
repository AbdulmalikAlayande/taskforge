package app.bola.taskforge.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskEvent extends TaskForgeEvent {
	
	private String taskId;
	private String projectId;
	private EventType eventType;
	private final String sourceEntityType = "task";
	
	public TaskEvent(Object source) {
		super(source);
	}
	
	public enum EventType {
		
		TASK_DUE,
		TASK_CREATED,
		TASK_UPDATED,
		TASK_DELETED,
		TASK_ASSIGNED,
		TASK_COMPLETED,
	}
}
