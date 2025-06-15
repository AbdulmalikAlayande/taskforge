package app.bola.taskforge.service;

import app.bola.taskforge.service.dto.TaskRequest;
import app.bola.taskforge.service.dto.TaskResponse;
import org.springframework.lang.NonNull;

public class TaskForgeTaskService implements TaskService{
	
	
	@Override
	public TaskResponse createNew(@NonNull TaskRequest taskRequest) {
		return null;
	}
	
	@Override
	public TaskResponse update(String publicId, @NonNull TaskRequest taskRequest) {
		return null;
	}
}
