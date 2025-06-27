package app.bola.taskforge.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskEvent extends TaskForgeEvent {
	
	private String taskId;
	private String projectId;
	private String oldValue;
	private String newValue;
	private final String sourceEntityType = "task";
	
	public TaskEvent(Object source) {
		super(source);
	}
}
