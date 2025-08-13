package app.bola.taskforge.service;

import app.bola.taskforge.common.service.BaseService;
import app.bola.taskforge.domain.entity.Task;
import app.bola.taskforge.service.dto.CommentResponse;
import app.bola.taskforge.service.dto.TaskRequest;
import app.bola.taskforge.service.dto.TaskResponse;
import app.bola.taskforge.service.dto.TaskUpdateRequest;

import java.util.Set;

public interface TaskService extends BaseService<TaskRequest, Task, TaskResponse> {
	
	TaskResponse assignMember(String taskId, String memberId);
	TaskResponse update(String taskId, TaskUpdateRequest updateRequest);
	CommentResponse getCommentThread(String taskId);
	Set<TaskResponse> findAll();
	
	Set<TaskResponse> getProjectSpecificTasks(String projectId);
}
