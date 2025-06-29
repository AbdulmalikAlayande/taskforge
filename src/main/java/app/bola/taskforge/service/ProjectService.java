package app.bola.taskforge.service;

import app.bola.taskforge.common.service.BaseService;
import app.bola.taskforge.domain.entity.Project;
import app.bola.taskforge.domain.enums.ProjectStatus;
import app.bola.taskforge.service.dto.ProjectRequest;
import app.bola.taskforge.service.dto.ProjectResponse;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Set;

public interface ProjectService extends BaseService<ProjectRequest, Project, ProjectResponse> {
	
	ProjectResponse addMember(@NonNull String projectId, @NonNull String memberId);

	ProjectResponse removeMember(@NonNull String projectId, @NonNull String memberId);
	
	ProjectResponse changeStatus(@NonNull String projectId, @NonNull String status);
	Set<ProjectResponse> getAllByOrganizationId(String organizationId);
	Set<ProjectResponse> findAll();
	ProjectResponse findById(String publicId);
	void delete(String publicId);
//	Object getActivityLog(String projectId);
}
