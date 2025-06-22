package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.entity.Project;
import app.bola.taskforge.domain.entity.Task;
import app.bola.taskforge.domain.enums.TaskStatus;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.repository.ProjectRepository;
import app.bola.taskforge.repository.TaskRepository;
import app.bola.taskforge.service.dto.TaskRequest;
import app.bola.taskforge.service.dto.TaskResponse;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
		
		validateTaskDateRange(taskRequest.getStartDate(), taskRequest.getDueDate());
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
	
	private void validateTaskDateRange(@NotNull LocalDate startDate, @NotNull LocalDate dueDate) {
		if (startDate == null || dueDate == null) {
			throw new IllegalArgumentException("Start date and due date cannot be null");
		}
		if (startDate.isAfter(dueDate)) {
			throw new InvalidRequestException("Start date is after due date");
		}
	}
	
	@Override
	public TaskResponse toResponse(Task entity) {
		return modelMapper.map(entity, TaskResponse.class);
	}
	
	@Override
	public TaskResponse update(String publicId, @NonNull TaskRequest taskRequest) {
		
		Task task = taskRepository.findByIdScoped(publicId)
			.orElseThrow(() -> new EntityNotFoundException("Task not found"));
		
		if (task.getStatus() == TaskStatus.DONE) {
			throw new InvalidRequestException("Cannot update a completed task");
		}
		if (StringUtils.isNotBlank(taskRequest.getTitle())) {
			task.setTitle(taskRequest.getTitle());
		}
		
		if (StringUtils.isNotBlank(taskRequest.getDescription())) {
			task.setDescription(taskRequest.getDescription());
		}
		
		if (taskRequest.getPriority() != null) {
			task.setPriority(taskRequest.getPriority());
		}
		
		if (taskRequest.getCategory() != null) {
			task.setCategory(taskRequest.getCategory());
		}
		
		if (taskRequest.getDueDate() != null && taskRequest.getStartDate() != null) {
			validateTaskDateRange(taskRequest.getStartDate(), taskRequest.getDueDate());
			task.setDueDate(taskRequest.getDueDate());
			task.setStartDate(taskRequest.getStartDate());
		}
		
		if (taskRequest.getStartDate() != null && taskRequest.getDueDate() == null) {
			validateTaskDateRange(taskRequest.getStartDate(), task.getDueDate());
			task.setStartDate(taskRequest.getStartDate());
		}
		
		if (taskRequest.getDueDate() != null && taskRequest.getStartDate() == null) {
			validateTaskDateRange(task.getStartDate(), taskRequest.getDueDate());
			task.setDueDate(taskRequest.getDueDate());
		}
		
		Task updatedTask = taskRepository.save(task);
		return toResponse(updatedTask);
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
