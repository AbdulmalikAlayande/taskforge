package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.dto.TaskRequest;
import app.bola.taskforge.service.dto.TaskResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/task")
public class TaskController implements BaseController<TaskRequest, TaskResponse> {
	
	@Override
	public ResponseEntity<TaskResponse> createNew(TaskRequest taskRequest) {
		return BaseController.super.createNew(taskRequest);
	}
}
