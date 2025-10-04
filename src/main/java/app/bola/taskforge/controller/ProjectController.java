package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.ProjectService;
import app.bola.taskforge.service.dto.ProjectRequest;
import app.bola.taskforge.service.dto.ProjectResponse;
import app.bola.taskforge.service.dto.MemberResponse;
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
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Set;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/project")
@Tag(name = "Project Management", description = "APIs for managing projects")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController implements BaseController<ProjectRequest, ProjectResponse> {
	
	private final ProjectService projectService;
	
	@Override
	@PostMapping("create-new")
	@Operation(summary = "Create a new project", description = "Creates a new project with the provided details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Project created successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponse.class))),
		@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
	})
	public ResponseEntity<ProjectResponse> createNew(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Project details", required = true)
			@RequestBody ProjectRequest request) {
		return ResponseEntity.ok(projectService.createNew(request));
	}
	
	@Override
	@GetMapping("{publicId}")
	@Operation(summary = "Get project by ID", description = "Retrieves project details by its public ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Project found",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "404", description = "Project not found", content = @Content)
	})
	public ResponseEntity<ProjectResponse> getById(
			@Parameter(description = "Public ID of the project", required = true)
			@PathVariable String publicId) {
		return ResponseEntity.ok(projectService.findById(publicId));
	}
	
	@Override
	@GetMapping("all")
	@Operation(summary = "Get all projects", description = "Retrieves all projects the user has access to")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "List of projects",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
	})
	public ResponseEntity<Collection<ProjectResponse>> getAll() {
		return ResponseEntity.ok(projectService.findAll());
	}
	
	@PutMapping("/{publicId}")
	@Operation(summary = "Update project", description = "Updates an existing project with new details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Project updated successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
		@ApiResponse(responseCode = "404", description = "Project not found", content = @Content)
	})
	public ResponseEntity<ProjectResponse> update(
			@Parameter(description = "Public ID of the project", required = true) @PathVariable String publicId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated project details", required = true)
			@RequestBody ProjectRequest request) {
		return ResponseEntity.ok(projectService.update(publicId, request));
	}
	
	@Override
	@DeleteMapping("/{publicId}")
	@Operation(summary = "Delete project", description = "Deletes a project by its public ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Project deleted successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
		@ApiResponse(responseCode = "404", description = "Project not found", content = @Content)
	})
	public ResponseEntity<Void> delete(
			@Parameter(description = "Public ID of the project", required = true)
			@PathVariable String publicId) {
		projectService.delete(publicId);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("add-member/{projectId}/{memberId}")
	@Operation(summary = "Add member to project", description = "Adds a member to a project")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Member added successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
		@ApiResponse(responseCode = "404", description = "Project or member not found", content = @Content)
	})
	public ResponseEntity<ProjectResponse> addMember(
			@Parameter(description = "ID of the project", required = true) @PathVariable String projectId,
			@Parameter(description = "ID of the member to add", required = true) @PathVariable String memberId) {
		return ResponseEntity.ok(projectService.addMember(projectId, memberId));
	}
	
	@DeleteMapping("remove-member/{projectId}/{memberId}")
	@Operation(summary = "Remove member from project", description = "Removes a member from a project")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Member removed successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
		@ApiResponse(responseCode = "404", description = "Project or member not found", content = @Content)
	})
	public ResponseEntity<ProjectResponse> removeMember(
			@Parameter(description = "ID of the project", required = true) @PathVariable String projectId,
			@Parameter(description = "ID of the member to remove", required = true) @PathVariable String memberId) {
		return ResponseEntity.ok(projectService.removeMember(projectId, memberId));
	}
	
	@PatchMapping("change-status/{projectId}/{status}")
	@Operation(summary = "Change project status", description = "Updates the status of a project")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Status updated successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
		@ApiResponse(responseCode = "404", description = "Project not found", content = @Content)
	})
	public ResponseEntity<ProjectResponse> changeStatus(
			@Parameter(description = "ID of the project", required = true) @PathVariable String projectId,
			@Parameter(description = "New status for the project", required = true) @PathVariable String status) {
		return ResponseEntity.ok(projectService.changeStatus(projectId, status));
	}
	
	@GetMapping("{organizationId}/projects")
	@Operation(summary = "Get projects by organization", description = "Retrieves all projects for a specific organization")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "List of projects",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "404", description = "Organization not found", content = @Content)
	})
	public ResponseEntity<Set<ProjectResponse>> getProjectsByOrganization(
			@Parameter(description = "ID of the organization", required = true)
			@PathVariable String organizationId) {
		return ResponseEntity.ok(projectService.getAllByOrganizationId(organizationId));
	}
	
	
	@GetMapping("{projectId}/members")
	@Operation(summary = "Get all members in project", description = "Retrieves all members for a specific project")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "List of members",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "404", description = "Project not found", content = @Content)
	})
	public ResponseEntity<Set<MemberResponse>> getProjectMembers(
			@Parameter(description = "ID of the project", required = true)
			@PathVariable String projectId) {
		return ResponseEntity.ok(projectService.getProjectMembers(projectId));
	}
}
