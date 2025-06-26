package app.bola.taskforge.event;

public class TaskEvent extends AppEvent{
	
	private String taskId;
	private String projectId;
	private String oldValue;
	private String newValue;
}
