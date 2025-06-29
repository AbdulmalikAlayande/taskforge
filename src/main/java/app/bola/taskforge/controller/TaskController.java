package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.TaskService;
import app.bola.taskforge.service.dto.TaskRequest;
import app.bola.taskforge.service.dto.TaskResponse;
import app.bola.taskforge.service.dto.TaskUpdateRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collection;

@RestController
@AllArgsConstructor
@RequestMapping("api/task")
public class TaskController implements BaseController<TaskRequest, TaskResponse> {
	
	
	private final TaskService taskService;
	
	@Override
	public ResponseEntity<TaskResponse> createNew(TaskRequest taskRequest) {
		return BaseController.super.createNew(taskRequest);
	}
	
	@Override
	public ResponseEntity<TaskResponse> getById(@PathVariable String publicId) {
		return ResponseEntity.ok(taskService.findById(publicId));
	}
	
	@Override
	public ResponseEntity<Collection<TaskResponse>> getAll() {
		return ResponseEntity.ok(taskService.findAll());
	}
	
	@PutMapping("/{publicId}")
	public ResponseEntity<TaskResponse> update(@PathVariable String publicId, @RequestBody TaskRequest request) {
		return ResponseEntity.ok(taskService.update(publicId, request));
	}
	
	@PatchMapping("/{publicId}")
	public ResponseEntity<TaskResponse> partialUpdate(@PathVariable String publicId, @RequestBody TaskUpdateRequest request) {
		return ResponseEntity.ok(taskService.update(publicId, request));
	}
	
	@Override
	public ResponseEntity<Void> delete(@PathVariable String publicId) {
		taskService.delete(publicId);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/{taskId}/assign/{memberId}")
	public ResponseEntity<TaskResponse> assignMember(@PathVariable String taskId, @PathVariable String memberId) {
		return ResponseEntity.ok(taskService.assignMember(taskId, memberId));
	}
}
