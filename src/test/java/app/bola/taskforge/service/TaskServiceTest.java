package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.*;
import app.bola.taskforge.domain.enums.TaskCategory;
import app.bola.taskforge.domain.enums.TaskPriority;
import app.bola.taskforge.domain.enums.TaskStatus;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.repository.ProjectRepository;
import app.bola.taskforge.repository.TaskRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.service.dto.MemberResponse;
import app.bola.taskforge.service.dto.TaskRequest;
import app.bola.taskforge.service.dto.TaskResponse;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(value = MockitoExtension.class)
public class TaskServiceTest {
	
	@Mock
	private TaskRepository taskRepository;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private OrganizationRepository organizationRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private Validator validator;
	@Mock
	private ModelMapper modelMapper;
	
	@InjectMocks
	private TaskForgeTaskService taskService;
	
	
	// Test data
	TaskRequest taskRequest;
	Project project;
	Organization organization;
	
	@BeforeEach
	public void setUp() {
		String organizationId = UUID.randomUUID().toString();
		organization = Organization.builder()
				               .publicId(organizationId).name("Mock Organization").slug("mock-org").description("A mock Org")
				               .phone("+2347023456789").email("mockorgemail@gmail.com").industry("Technology").build();
		
		String projectId = UUID.randomUUID().toString();
		project = Project.builder().name("Mock Project").publicId(projectId)
				          .dateRange(new DateRange(LocalDate.now().plusDays(2), LocalDate.now().plusDays(5)))
				          .build();
		
		taskRequest = TaskRequest.builder()
				              .title("Mock Task").description("Mock Description").projectId(projectId).organizationId(organizationId)
				              .dueDate(LocalDate.now().plusDays(3)).startDate(LocalDate.now().plusDays(1))
				              .priority(TaskPriority.LOW).category(TaskCategory.FEATURE).build();
	}
	
	@Nested
	@DisplayName("Create New Task Tests")
	public class CreateNewTaskTests {
		
		@Test
		@DisplayName("should create task successfully given valid request and all checks passed")
		public void shouldCreateTaskSuccessfullyWhenAndAllChecksPassed() {
			
			Task task = Task.builder()
				.title("Mock Task").description("Mock Description").project(project).organization(organization)
				.startDate(LocalDate.now().plusDays(1)).dueDate(LocalDate.now().plusDays(3))
	            .priority(TaskPriority.LOW).category(TaskCategory.FEATURE).build();
			
			when(modelMapper.map(taskRequest, Task.class)).thenReturn(task);
			when(validator.validate(taskRequest)).thenReturn(Set.of());
			when(modelMapper.map(task, TaskResponse.class)).thenReturn(new TaskResponse());
			when(organizationRepository.findByIdScoped(any())).thenReturn(Optional.of(organization));
			when(projectRepository.findByIdScoped(project.getPublicId())).thenReturn(Optional.of(project));
			when(taskRepository.save(task)).thenReturn(task);
			when(modelMapper.map(task, TaskResponse.class)).thenReturn(TaskResponse.builder().title("Mock Task").build());
			
			TaskResponse response = taskService.createNew(taskRequest);

			assertNotNull(response);
			assertEquals("Mock Task", response.getTitle());
			verify(taskRepository).save(any(Task.class));
		}
		
		@Test
		@DisplayName("Should throw NullPointerException when the request object is null")
		public void shouldFailWhenRequestIsNull() {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> taskService.createNew(null));
			assertEquals("Request cannot be null", exception.getMessage());
		}
		
		@Test
		@DisplayName("should throw InvalidRequestException when required fields are missing")
		public void shouldFailWhenMissingRequiredFields() {
			
			TaskRequest request = TaskRequest.builder()
				.title("Mock Task").description("Mock Description").build();
			
			when(validator.validate(request)).thenThrow(new InvalidRequestException("Required field are missing"));
			
			InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> taskService.createNew(request));
			assertEquals("Required field are missing", exception.getMessage());
		}
		
		@Test
		@DisplayName("should throw InvalidRequestException when due date is before start date")
		public void shouldFailWhenDueDateIsBeforeStartDate() {
			taskRequest.setDueDate(LocalDate.now().minusDays(4));
			when(validator.validate(taskRequest)).thenThrow(new InvalidRequestException("Start date is after due date"));
			
			InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> taskService.createNew(taskRequest));
			assertEquals("Start date is after due date", exception.getMessage());
		}
		
		@Test
		@DisplayName("should throw EntityNotFoundException when the project does not exist")
		public void shouldFailGivenNonExistentProjectId() {
			String nonExistentProjectId = UUID.randomUUID().toString();
			taskRequest.setProjectId(nonExistentProjectId);
			
			when(organizationRepository.findByIdScoped(any())).thenReturn(Optional.of(organization));
			when(projectRepository.findByIdScoped(nonExistentProjectId)).thenReturn(Optional.empty());
			
			EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> taskService.createNew(taskRequest));
			assertEquals("Project not found", exception.getMessage());
		}
		
	}
	
	@Nested
	@DisplayName("Update Task Tests")
	public class UpdateTaskTests {
		
		Task task;
		TaskResponse response;
		
		@BeforeEach
		public void setUp(){
			 task = Task.builder()
			            .title("Mock Task").description("Mock Description").project(project).organization(organization)
			            .startDate(LocalDate.now().plusDays(1)).dueDate(LocalDate.now().plusDays(3))
			            .priority(TaskPriority.LOW).category(TaskCategory.FEATURE).build();
			
			when(modelMapper.map(taskRequest, Task.class)).thenReturn(task);
			when(validator.validate(taskRequest)).thenReturn(Set.of());
			when(organizationRepository.findByIdScoped(any())).thenReturn(Optional.of(organization));
			when(projectRepository.findByIdScoped(project.getPublicId())).thenReturn(Optional.of(project));
			when(taskRepository.save(task)).thenReturn(task);
			when(modelMapper.map(any(Task.class), eq(TaskResponse.class))).thenAnswer(invocation -> {
				Task taskArg = invocation.getArgument(0);
				return TaskResponse.builder()
					.title(taskArg.getTitle()).description(taskArg.getDescription()).dueDate(taskArg.getDueDate())
					.startDate(taskArg.getStartDate()).priority(taskArg.getPriority()).category(taskArg.getCategory()).build();
			});
			response = taskService.createNew(taskRequest);
		}
		
		@Test
		@DisplayName("should update a task successfully")
		public void shouldUpdateTaskSuccessfully() {
			
			TaskRequest updateRequest = TaskRequest.builder()
					.title("Updated Task Title")
					.description("Updated Description")
					.dueDate(LocalDate.now().plusDays(4))
					.startDate(LocalDate.now().plusDays(2))
					.priority(TaskPriority.HIGH)
					.category(TaskCategory.BUG)
					.build();
			
			when(taskRepository.findByIdScoped(response.getPublicId())).thenReturn(Optional.of(task));
			
			String initialTitle = response.getTitle();
			TaskCategory initialCategory = response.getCategory();
			response = taskService.update(response.getPublicId(), updateRequest);
			assertNotNull(response);
			
			String updatedTitle = response.getTitle();
			TaskCategory updatedCategory = response.getCategory();
			assertNotEquals(initialTitle,updatedTitle);
			assertNotEquals(initialCategory, updatedCategory);
			assertEquals("Updated Task Title", response.getTitle());
			assertEquals(TaskPriority.HIGH, response.getPriority());
			assertEquals(TaskCategory.BUG, response.getCategory());
		}
		
		@Test
		@DisplayName("should allow partial updates")
		public void shouldAllowPartialUpdates() {
			TaskRequest partialUpdateRequest = TaskRequest.builder()
					.title("Partially Updated Task Title")
					.build();
			
			when(taskRepository.findByIdScoped(response.getPublicId())).thenReturn(Optional.of(task));
			
			String initialTitle = response.getTitle();
			TaskCategory initialCategory = response.getCategory();
			response = taskService.update(response.getPublicId(), partialUpdateRequest);
			assertNotNull(response);
			
			String updatedTitle = response.getTitle();
			TaskCategory updatedCategory = response.getCategory();
			assertNotEquals(initialTitle, updatedTitle);
			assertEquals("Partially Updated Task Title", response.getTitle());
			assertEquals(initialCategory, updatedCategory);
		}
		
		
		@Test
		@DisplayName("should throw EntityNotFoundException if taskId does not exist")
		public void shouldThrowEntityNotFoundExceptionIfTaskIdDoesNotExist() {
			String nonExistentTaskId = UUID.randomUUID().toString();
			when(taskRepository.findByIdScoped(nonExistentTaskId)).thenReturn(Optional.empty());
			
			EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> taskService.update(nonExistentTaskId, taskRequest));
			
			assertEquals("Task not found", exception.getMessage());
		}
		
		@Test
		@DisplayName("should fail if due date is in the past")
		public void shouldFailIfDueDateIsInThePast() {
			TaskRequest invalidDateRequest = TaskRequest.builder()
					                                 .dueDate(LocalDate.now().minusDays(1))
					                                 .build();
			
			when(taskRepository.findByIdScoped(response.getPublicId())).thenReturn(Optional.of(task));
			
			Optional<Task> optionalTask = taskRepository.findByIdScoped(response.getPublicId());
			assertTrue(optionalTask.isPresent(), "Expected task to be present");
			
			Task task = optionalTask.get();
			LocalDate originalDueDate = task.getDueDate();
			
			assertThrows(InvalidRequestException.class, () -> taskService.update(response.getPublicId(), invalidDateRequest));
			
			assertEquals(originalDueDate, task.getDueDate());
		}
		
		
		@Test
		@DisplayName("should fail if task is already marked as 'Done'")
		public void shouldFailIfTaskIsAlreadyMarkedAsDone() {
			Task doneTask = Task.builder()
					                .title("Done Task")
					                .status(TaskStatus.DONE)
					                .build();
			
			when(taskRepository.findByIdScoped(response.getPublicId())).thenReturn(Optional.of(doneTask));
			
			InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> taskService.update(response.getPublicId(), taskRequest));
			
			assertEquals("Cannot update a completed task", exception.getMessage());
		}
		
		@Test
		@DisplayName("should update only provided fields while keeping others unchanged")
		public void shouldUpdateOnlyProvidedFields() {
			when(taskRepository.findByIdScoped(response.getPublicId())).thenReturn(Optional.of(task));
			
			Optional<Task> optionalTask = taskRepository.findByIdScoped(response.getPublicId());
			assertTrue(optionalTask.isPresent(), "Expected task to be present");
			Task originalTask = optionalTask.get();
			
			String originalTitle = originalTask.getTitle();
			String originalDescription = originalTask.getDescription();
			LocalDate originalStartDate = originalTask.getStartDate();
			
			TaskRequest updateRequest = TaskRequest.builder().priority(TaskPriority.HIGH).build();
			
			TaskResponse updatedResponse = taskService.update(response.getPublicId(), updateRequest);
			
			assertEquals(TaskPriority.HIGH, updatedResponse.getPriority());
			assertEquals(originalTitle, updatedResponse.getTitle());
			assertEquals(originalDescription, updatedResponse.getDescription());
			assertEquals(originalStartDate, updatedResponse.getStartDate());
		}
		
		@Test
		@DisplayName("should validate date consistency when only start date is provided")
		public void shouldValidateDateConsistencyWhenOnlyStartDateProvided() {
			TaskRequest invalidRequest = TaskRequest.builder().startDate(LocalDate.now().plusDays(10)).build();
			
			when(taskRepository.findByIdScoped(response.getPublicId())).thenReturn(Optional.of(task));
			
			Optional<Task> optionalTask = taskRepository.findByIdScoped(response.getPublicId());
			LocalDate originalDueDate;
			if (optionalTask.isPresent()) {
				originalDueDate = optionalTask.get().getDueDate();
				
				assertThrows(InvalidRequestException.class, () -> taskService.update(response.getPublicId(), invalidRequest));
			
				assertEquals(originalDueDate, task.getDueDate());
			}
			else {
				fail("Expected task to be present, but was empty");
			}
		}
		
		@Test
		@DisplayName("should validate date consistency when only due date is provided")
		public void shouldValidateDateConsistencyWhenOnlyDueDateProvided() {
			TaskRequest invalidRequest = TaskRequest.builder()
					                             .dueDate(LocalDate.now().minusDays(1))
					                             .build();
			
			when(taskRepository.findByIdScoped(response.getPublicId())).thenReturn(Optional.of(task));
			
			Optional<Task> optionalTask = taskRepository.findByIdScoped(response.getPublicId());
			LocalDate originalStartDate;
			
			if (optionalTask.isPresent()) {
				originalStartDate = optionalTask.get().getStartDate();
				assertThrows(InvalidRequestException.class, () -> taskService.update(response.getPublicId(), invalidRequest));
				assertEquals(originalStartDate, task.getStartDate());
			}
			else {
				fail("Expected task to be present, but was empty");
			}
		}
	}
	
	@Nested
	@DisplayName("Assign member to task tests")
	class AssignMemberTests {
		
		Task task;
		TaskResponse response;
		
		@BeforeEach
		public void setUp() {
			String taskId = UUID.randomUUID().toString();
			
			task = Task.builder()
		            .title("Mock Task").description("Mock Description").project(project).organization(organization)
		            .startDate(LocalDate.now().plusDays(1)).dueDate(LocalDate.now().plusDays(3))
		            .priority(TaskPriority.LOW).status(TaskStatus.IN_PROGRESS).category(TaskCategory.FEATURE)
		            .publicId(taskId).build();
			
			when(modelMapper.map(taskRequest, Task.class)).thenReturn(task);
			when(validator.validate(taskRequest)).thenReturn(Set.of());
			when(organizationRepository.findByIdScoped(any())).thenReturn(Optional.of(organization));
			when(projectRepository.findByIdScoped(project.getPublicId())).thenReturn(Optional.of(project));
			when(taskRepository.save(task)).thenReturn(task);
			when(modelMapper.map(any(Task.class), eq(TaskResponse.class))).thenAnswer(invocation -> {
				Task taskArg = invocation.getArgument(0);
				return TaskResponse.builder()
						       .title(taskArg.getTitle()).description(taskArg.getDescription())
						       .dueDate(taskArg.getDueDate()).startDate(taskArg.getStartDate())
						       .priority(taskArg.getPriority()).category(taskArg.getCategory())
						       .status(TaskStatus.IN_PROGRESS).build();
			});
			
			response = taskService.createNew(taskRequest);
			
		}
		
		@Test
		@DisplayName("Should assign member to valid task successfully")
		public void shouldAssignMemberToTaskSuccessfully() {
			String memberId = UUID.randomUUID().toString();
			
			Member member = Member.builder().publicId(memberId).email("testmember@gmail.com").build();
			
			assertNull(response.getAssignee());
			
			when(taskRepository.findByIdScoped(response.getPublicId())).thenReturn(Optional.of(task));
			when(userRepository.findByIdScoped(memberId)).thenReturn(Optional.of(member));
			when(modelMapper.map(any(Task.class), eq(TaskResponse.class))).thenAnswer(invocation -> {
				Task taskArg = invocation.getArgument(0);
				return TaskResponse.builder()
						       .category(taskArg.getCategory()).status(TaskStatus.IN_PROGRESS).title(taskArg.getTitle())
						       .assignee(MemberResponse.builder().publicId(memberId).email("testmember@gmail.com").build())
						       .dueDate(taskArg.getDueDate()).startDate(taskArg.getStartDate()).priority(taskArg.getPriority())
						       .description(taskArg.getDescription()).build();
			});
			
			response = taskService.assignMember(response.getPublicId(), memberId);
			
			assertNotNull(response.getAssignee());
			assertEquals(response.getAssignee().getEmail(), member.getEmail());
			assertEquals(response.getAssignee().getPublicId(), member.getPublicId());
		}
		
		@Test
		@DisplayName("Should throw InvalidRequest exception if the task is completed")
		public void shouldThrowExceptionIfTaskIsMarkedDone_Completed(){
			String memberId = UUID.randomUUID().toString();
			
			task.setStatus(TaskStatus.DONE);
			when(taskRepository.findByIdScoped(response.getPublicId())).thenReturn(Optional.of(task));
			
			Exception exception = assertThrows(InvalidRequestException.class,
					()->taskService.assignMember(response.getPublicId(), memberId));
			
			String expectedErrorMessage = "Task is already completed, you can't assign a member to an already completed task";
			assertEquals(expectedErrorMessage, exception.getMessage());
		}
		
		@Test
		@DisplayName("Should throw InvalidRequestException if no task associating with the provided Id is found")
		public void shouldThrowExceptionIfTaskIsNotFoundWithProvidedId(){
			String memberId = UUID.randomUUID().toString();
			String randomTaskId = UUID.randomUUID().toString();
			
			when(taskRepository.findByIdScoped(randomTaskId)).thenReturn(Optional.empty());
			
			Exception exception = assertThrows(EntityNotFoundException.class,
					()->taskService.assignMember(randomTaskId, memberId));
			
			assertEquals("Task not found", exception.getMessage());
		}
		
		@Test
		@DisplayName("Should fail if the member does not exist")
		public void shouldFailIfMemberDoesNotExist() {
			String nonExistentMemberId = UUID.randomUUID().toString();
			when(taskRepository.findByIdScoped(response.getPublicId())).thenReturn(Optional.of(task));
			when(userRepository.findByIdScoped(nonExistentMemberId)).thenReturn(Optional.empty());
			
			EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
					() -> taskService.assignMember(response.getPublicId(), nonExistentMemberId));
			
			assertEquals("Member not found", exception.getMessage());
		}
		
		@Test
		@DisplayName("Should fail if the task is archived or deleted")
		public void shouldFailIfTaskIsArchivedOrDeleted() {
			String memberId = UUID.randomUUID().toString();
			
			task.setStatus(TaskStatus.ARCHIVED);
			
			when(taskRepository.findByIdScoped(response.getPublicId())).thenReturn(Optional.of(task));
			
			InvalidRequestException exception = assertThrows(InvalidRequestException.class,
					() -> taskService.assignMember(response.getPublicId(), memberId));
			
			assertEquals("Task is archived, you can't assign a member to an archived task", exception.getMessage());
		}
		
		@Test
		@DisplayName("Should fail if the member is already assigned to the task")
		public void shouldFailIfMemberAlreadyAssigned() {
			String memberId = UUID.randomUUID().toString();
			Member member = Member.builder().publicId(memberId).email("already@assigned.com").build();
			
			// Simulate already assigned
			task.setAssignee(member);
			
			when(taskRepository.findByIdScoped(response.getPublicId())).thenReturn(Optional.of(task));
			
			InvalidRequestException exception = assertThrows(InvalidRequestException.class,
					() -> taskService.assignMember(response.getPublicId(), memberId));
			
			assertEquals("Member is already assigned to this task", exception.getMessage());
		}
	}
}
