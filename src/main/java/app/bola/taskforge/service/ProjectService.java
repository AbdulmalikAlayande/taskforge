package app.bola.taskforge.service;

import app.bola.taskforge.common.service.BaseService;
import app.bola.taskforge.domain.entity.Project;
import app.bola.taskforge.service.dto.ProjectRequest;
import app.bola.taskforge.service.dto.ProjectResponse;

import java.util.Set;

public interface ProjectService extends BaseService<ProjectRequest, Project, ProjectResponse> {
	
	ProjectResponse addMember(String projectId, String memberId);

	ProjectResponse removeMember(String projectId, String memberId);
	
//	ProjectResponse update(ProjectRequest request);
	
	Set<ProjectResponse> getAllByOrganizationId(String organizationId);
	
//	Object getActivityLog(String projectId);
}
