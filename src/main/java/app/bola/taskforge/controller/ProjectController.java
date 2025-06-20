package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.ProjectService;
import app.bola.taskforge.service.dto.ProjectRequest;
import app.bola.taskforge.service.dto.ProjectResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/project")
public class ProjectController implements BaseController<ProjectRequest, ProjectResponse> {
	
	private final ProjectService projectService;
	
	@Override
	public ResponseEntity<ProjectResponse> createNew(ProjectRequest request) {
		return ResponseEntity.ok(projectService.createNew(request));
	}
}
