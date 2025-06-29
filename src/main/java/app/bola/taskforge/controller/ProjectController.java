package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.ProjectService;
import app.bola.taskforge.service.dto.ProjectRequest;
import app.bola.taskforge.service.dto.ProjectResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("api/project")
public class ProjectController implements BaseController<ProjectRequest, ProjectResponse> {
	
	private final ProjectService projectService;
	
	@Override
	public ResponseEntity<ProjectResponse> createNew(ProjectRequest request) {
		return ResponseEntity.ok(projectService.createNew(request));
	}
	
	@Override
	public ResponseEntity<ProjectResponse> getById(@PathVariable String publicId) {
		return ResponseEntity.ok(projectService.findById(publicId));
	}
	
	@Override
	public ResponseEntity<Collection<ProjectResponse>> getAll() {
		return ResponseEntity.ok(projectService.findAll());
	}
	
	public ResponseEntity<ProjectResponse> update(@PathVariable String publicId, @RequestBody ProjectRequest request) {
		return ResponseEntity.ok(projectService.update(publicId, request));
	}
	
	@Override
	public ResponseEntity<Void> delete(@PathVariable String publicId) {
		projectService.delete(publicId);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("add-member/{projectId}/{memberId}")
	public ResponseEntity<ProjectResponse> addMember(@PathVariable String projectId, @PathVariable String memberId) {
		return ResponseEntity.ok(projectService.addMember(projectId, memberId));
	}
	
	@DeleteMapping("remove-member/{projectId}/{memberId}")
	public ResponseEntity<ProjectResponse> removeMember(@PathVariable String projectId, @PathVariable String memberId) {
		return ResponseEntity.ok(projectService.removeMember(projectId, memberId));
	}
	
	@PatchMapping("change-status/{projectId}/{status}")
	public ResponseEntity<ProjectResponse> changeStatus(@PathVariable String projectId, @PathVariable String status) {
		return ResponseEntity.ok(projectService.changeStatus(projectId, status));
	}
	
	@GetMapping("{organizationId}/projects")
	public ResponseEntity<Set<ProjectResponse>> getProjectsByOrganization(@PathVariable String organizationId) {
		return ResponseEntity.ok(projectService.getAllByOrganizationId(organizationId));
	}
}
