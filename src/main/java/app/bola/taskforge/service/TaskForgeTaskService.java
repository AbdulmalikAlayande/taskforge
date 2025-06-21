package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.entity.Project;
import app.bola.taskforge.domain.entity.Task;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.repository.ProjectRepository;
import app.bola.taskforge.repository.TaskRepository;
import app.bola.taskforge.service.dto.TaskRequest;
import app.bola.taskforge.service.dto.TaskResponse;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class TaskForgeTaskService implements TaskService{
	
	final Validator validator;
	private final ModelMapper modelMapper;
	private final TaskRepository taskRepository;
	private final OrganizationRepository organizationRepository;
	private final ProjectRepository projectRepository;
	
	
	@Override
	public TaskResponse createNew(@NonNull TaskRequest taskRequest) {
		
		performValidation(validator, taskRequest);
		
		Organization organization = organizationRepository.findByIdScoped(taskRequest.getOrganizationId())
			.orElseThrow(() -> new EntityNotFoundException("Organization not found"));
		
		Project project = projectRepository.findByIdScoped(taskRequest.getProjectId())
			.orElseThrow(() -> new EntityNotFoundException("Project not found"));
		
		Task task = modelMapper.map(taskRequest, Task.class);
		task.setOrganization(organization);
		task.setProject(project);
		
		Task savedTask = taskRepository.save(task);
		TaskResponse response = toResponse(savedTask);
		response.setOrganizationId(organization.getPublicId());
		response.setProjectId(project.getPublicId());
		return response;
	}
	
	@Override
	public TaskResponse toResponse(Task entity) {
		return modelMapper.map(entity, TaskResponse.class);
	}
	
	@Override
	public TaskResponse update(String publicId, @NonNull TaskRequest taskRequest) {
		return null;
	}
	
	@Override
	public TaskResponse findById(String publicId) {
		return null;
	}
	
	@Override
	public Set<TaskResponse> findAll() {
		return Set.of();
	}
	
	@Override
	public void deleteById(String publicId) {
	
	}
	
	@Override
	public TaskResponse assignMember(String taskId, String memberId) {
		return null;
	}
}
