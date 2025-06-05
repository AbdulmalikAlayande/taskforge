package app.bola.taskforge.service;

import app.bola.taskforge.common.service.BaseService;
import app.bola.taskforge.domain.entity.Task;
import app.bola.taskforge.service.dto.TaskRequest;
import app.bola.taskforge.service.dto.TaskResponse;

public interface TaskService extends BaseService<TaskRequest, Task, TaskResponse> {

	// Define any additional methods specific to TaskService here
}
