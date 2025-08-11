package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.TaskService;
import app.bola.taskforge.service.dto.TaskRequest;
import app.bola.taskforge.service.dto.TaskResponse;
import app.bola.taskforge.service.dto.TaskUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;


@RestController
@AllArgsConstructor
@RequestMapping("api/task")
@Tag(name = "Task Management", description = "APIs for managing tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController implements BaseController<TaskRequest, TaskResponse> {
	
	
	private final TaskService taskService;
	
	@Override
	@PostMapping("create-new")
	@Operation(summary = "Create a new task", description = "Creates a new task with the provided details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Task created successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
		@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
	})
	public ResponseEntity<TaskResponse> createNew(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Task details", required = true)
			@RequestBody TaskRequest taskRequest) {
		return ResponseEntity.ok(taskService.createNew(taskRequest));
	}
	
	@Override
	@GetMapping("{publicId}")
	@Operation(summary = "Get task by ID", description = "Retrieves task details by its public ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Task found",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
	})
	public ResponseEntity<TaskResponse> getById(
			@Parameter(description = "Public ID of the task", required = true)
			@PathVariable String publicId) {
		return ResponseEntity.ok(taskService.findById(publicId));
	}
	
	@Override
	@GetMapping("all")
	@Operation(summary = "Get all tasks", description = "Retrieves all tasks the user has access to")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "List of tasks",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
	})
	public ResponseEntity<Collection<TaskResponse>> getAll() {
		return ResponseEntity.ok(taskService.findAll());
	}
	
	@PutMapping("/{publicId}")
	@Operation(summary = "Update task", description = "Updates an existing task with new details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Task updated successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
		@ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
	})
	public ResponseEntity<TaskResponse> update(
			@Parameter(description = "Public ID of the task", required = true) @PathVariable String publicId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated task details", required = true)
			@RequestBody TaskRequest request) {
		return ResponseEntity.ok(taskService.update(publicId, request));
	}
	
	@PatchMapping("/{publicId}")
	@PreAuthorize("hasRole('PROJECT_MANAGER') or @resourceSecurity.isTaskOwner(#publicId, authentication.name)")
	@Operation(
		summary = "Partially update task",
		description = "Updates specific fields of an existing task. Requires PROJECT_MANAGER role or task ownership."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Task updated successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Not authorized to update this task", content = @Content),
		@ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
	})
	public ResponseEntity<TaskResponse> partialUpdate(
			@Parameter(description = "Public ID of the task", required = true) @PathVariable String publicId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Fields to update", required = true)
			@RequestBody TaskUpdateRequest request) {
		return ResponseEntity.ok(taskService.update(publicId, request));
	}
	
	@Override
	@DeleteMapping("/{publicId}")
	@Operation(summary = "Delete task", description = "Deletes a task by its public ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Task deleted successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
		@ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
	})
	public ResponseEntity<Void> delete(
			@Parameter(description = "Public ID of the task", required = true)
			@PathVariable String publicId) {
		taskService.delete(publicId);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/{taskId}/assign/{memberId}")
	@PreAuthorize("hasRole('PROJECT_MANAGER')")
	@Operation(
		summary = "Assign member to task",
		description = "Assigns a member to a task. Requires PROJECT_MANAGER role."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Member assigned successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Not authorized to assign members", content = @Content),
		@ApiResponse(responseCode = "404", description = "Task or member not found", content = @Content)
	})
	public ResponseEntity<TaskResponse> assignMember(
			@Parameter(description = "ID of the task", required = true) @PathVariable String taskId,
			@Parameter(description = "ID of the member to assign", required = true) @PathVariable String memberId) {
		return ResponseEntity.ok(taskService.assignMember(taskId, memberId));
	}
}
