package app.bola.taskforge.service;

import app.bola.taskforge.domain.context.TenantContext;
import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.entity.Project;
import app.bola.taskforge.domain.entity.Task;
import app.bola.taskforge.domain.enums.TaskStatus;
import app.bola.taskforge.event.TaskEvent;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.repository.ProjectRepository;
import app.bola.taskforge.repository.TaskRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.service.dto.CommentResponse;
import app.bola.taskforge.service.dto.TaskRequest;
import app.bola.taskforge.service.dto.TaskResponse;
import app.bola.taskforge.service.dto.TaskUpdateRequest;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
	private final ApplicationEventPublisher eventPublisher;
	
	
	@Override
	public TaskResponse createNew(@NonNull TaskRequest taskRequest) {
		
		performValidation(validator, taskRequest);
		
		validateTaskDateRange(taskRequest.getStartDate(), taskRequest.getDueDate());
		Organization organization = organizationRepository.findByIdScoped(taskRequest.getOrganizationId())
			.orElseThrow(() -> new EntityNotFoundException("Organization not found"));
		
		Project project = projectRepository.findByIdScoped(taskRequest.getProjectId())
			.orElseThrow(() -> new EntityNotFoundException("Project not found"));
		
		Task task = modelMapper.map(taskRequest, Task.class);
		if (!taskRequest.getAssigneeId().isBlank()) {
			Optional<Member> member = userRepository.findByEmail(taskRequest.getAssigneeId());
			if (member.isPresent()) {
				task.setAssignee(member.get());
			}
			else {
				member = userRepository.findByIdScoped(taskRequest.getAssigneeId());
				if (member.isPresent()) {
					task.setAssignee(member.get());
				}
				else{
					log.warn("Invalid assignee ID passed for creation of task. ID: {}", taskRequest.getAssigneeId());
				}
			}
		}
		task.setOrganization(organization);
		task.setProject(project);
		task.setStatus(TaskStatus.TODO);

		
		Task savedTask = taskRepository.save(task);
		TaskResponse response = toResponse(savedTask);
		response.setOrganizationId(organization.getPublicId());
		response.setProjectId(project.getPublicId());
		
		TaskEvent event = buildTaskEvent(savedTask, project, organization.getId());
		eventPublisher.publishEvent(event);
		
		return response;
	}
	
	private static TaskEvent buildTaskEvent(Task savedTask, Project project, String organizationId) {
		
		TaskEvent event = new TaskEvent(savedTask);
		
		event.setTaskId(savedTask.getId());
		event.setProjectId(project.getId());
		event.setOrganizationId(organizationId);
		event.setDateTimeStamp(LocalDateTime.now());
		event.setEventType(TaskEvent.EventType.TASK_CREATED);
		event.setUserIdList(project.getMembers().stream().map(Member::getPublicId).collect(Collectors.toList()));
		event.setUserEmailList(project.getMembers().stream().map(Member::getEmail).filter(StringUtils::isNotBlank).toList());
		
		return event;
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
	public TaskResponse update(String taskId, TaskUpdateRequest updateRequest) {
		
		Task task = taskRepository.findByIdScoped(taskId)
				            .orElseThrow(() -> new EntityNotFoundException("Task not found"));
		
		if (task.getStatus() == TaskStatus.DONE) {
			throw new InvalidRequestException("Cannot update a completed task");
		}
		
		if (StringUtils.isNotBlank(updateRequest.title())) {
			task.setTitle(updateRequest.title());
		}
		
		if (StringUtils.isNotBlank(updateRequest.description())) {
			task.setDescription(updateRequest.description());
		}
		
		if (updateRequest.priority() != null) {
			task.setPriority(updateRequest.priority());
		}
		
		if (updateRequest.category() != null) {
			task.setCategory(updateRequest.category());
		}
		
		if (updateRequest.dueDate() != null && updateRequest.startDate() != null) {
			validateTaskDateRange(updateRequest.startDate(), updateRequest.dueDate());
			task.setDueDate(updateRequest.dueDate());
			task.setStartDate(updateRequest.startDate());
		}
		
		if (updateRequest.startDate() != null && updateRequest.dueDate() == null) {
			validateTaskDateRange(updateRequest.startDate(), task.getDueDate());
			task.setStartDate(updateRequest.startDate());
		}
		
		if (updateRequest.dueDate() != null && updateRequest.startDate() == null) {
			validateTaskDateRange(task.getStartDate(), updateRequest.dueDate());
			task.setDueDate(updateRequest.dueDate());
		}
		
		Task updatedTask = taskRepository.save(task);
		return toResponse(updatedTask);	}
	
	@Override
	public CommentResponse getCommentThread(String taskId) {
		return null;
	}
	
	@Override
	public TaskResponse update(String publicId, @NonNull TaskRequest taskRequest) {
		return update(publicId, modelMapper.map(taskRequest, TaskUpdateRequest.class));
	}
	
	@Override
	public TaskResponse findById(String publicId) {
		Task task = taskRepository.findByIdScoped(publicId)
				            .orElseThrow(() -> new EntityNotFoundException("Task not found"));
		return toResponse(task);
	}
	
	@Override
	public Set<TaskResponse> findAll() {
		List<Task> tasks = taskRepository.findAllScoped();
		return tasks.stream()
				       .map(this::toResponse)
				       .collect(Collectors.toSet());
	}
	
	@Override
	public Set<TaskResponse> getProjectSpecificTasks(String projectId) {
		return Set.of();
	}
	
	@Override
	public void delete(String publicId) {
		Task task = taskRepository.findByIdScoped(publicId)
				            .orElseThrow(() -> new EntityNotFoundException("Task not found"));
		taskRepository.deleteByIdScoped(publicId);
		taskRepository.delete(task);
		log.info("Task [{}] soft-deleted under tenant [{}]", publicId, TenantContext.getCurrentTenant());
	}
}
