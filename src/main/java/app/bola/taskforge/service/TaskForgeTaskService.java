package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.entity.Project;
import app.bola.taskforge.domain.entity.Task;
import app.bola.taskforge.domain.enums.TaskStatus;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.repository.ProjectRepository;
import app.bola.taskforge.repository.TaskRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.service.dto.TaskRequest;
import app.bola.taskforge.service.dto.TaskResponse;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class TaskForgeTaskService implements TaskService{
	
	final Validator validator;
	private final ModelMapper modelMapper;
	private final TaskRepository taskRepository;
	private final OrganizationRepository organizationRepository;
	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	
	
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
		task.setStatus(TaskStatus.TODO);
		
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
	public TaskResponse assignMember(String taskId, String memberId) {
		Task task = taskRepository.findByIdScoped(taskId)
				            .orElseThrow(() -> new EntityNotFoundException("Task not found"));
		
		if (task.isCompleted()) {
			throw new InvalidRequestException("Task is already completed, you can't assign a member to an already completed task");
		}
		
		if (task.isArchived()) {
			throw new InvalidRequestException("Task is archived, you can't assign a member to an archived task");
		}
		
		if (task.getAssignee() != null){
			if (task.getAssignee().getPublicId().equals(memberId)) {
				throw new InvalidRequestException("Member is already assigned to this task");
			}
			log.warn("Reassigning member {} to task {}", memberId, taskId);
			Member member = userRepository.findByIdScoped(memberId)
					                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
			task.setAssignee(member);
		}
		
		else {
			Member member = userRepository.findByIdScoped(memberId)
					                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
			task.setAssignee(member);
		}
		return toResponse(taskRepository.save(task));
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
}
