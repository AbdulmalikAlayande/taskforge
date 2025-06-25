package app.bola.taskforge.integration;

import app.bola.taskforge.domain.context.TenantContext;
import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.entity.Task;
import app.bola.taskforge.domain.enums.*;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.repository.TaskRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.service.OrganizationService;
import app.bola.taskforge.service.ProjectService;
import app.bola.taskforge.service.TaskService;
import app.bola.taskforge.service.dto.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Transactional
@SpringBootTest
public class TaskIntegrationTest {
	
	@Autowired
	private TaskService taskService;
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private ProjectService projectService;
	
	
	OrganizationResponse orgResponse;
	ProjectResponse projResponse;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private UserRepository userRepository;
	
	@BeforeEach
	public void beforeEach() {
		
		OrganizationRequest orgRequest = OrganizationRequest.builder()
				                                 .name("Test Organization").slug("test-org").industry("Healthcare").country("Test Country")
				                                 .description("This is a test organization").contactPhone("+2347056453241").contactEmail("testorg@gmail.com")
				                                 .timeZone("Africa/Lagos").websiteUrl("https://testorg.ng").logoUrl("https://testorg.ng/logo.png").build();
		
		orgResponse = organizationService.createNew(orgRequest);
		TenantContext.setCurrentTenant(orgResponse.getPublicId());
		
		ProjectRequest projRequest = ProjectRequest.builder()
				                             .organizationId(orgResponse.getPublicId()).name("Test Project").description("This is a test project")
				                             .startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(30))
				                             .category(ProjectCategory.SOFTWARE).memberIds(List.of()).build();
		
		projResponse = projectService.createNew(projRequest);
	}
	
	@Nested
	@DisplayName("Create New Task Tests")
	class CreateNewTaskTests {
		
		@Test
		@DisplayName("should create task successfully with valid input and persist task in database")
		public void shouldCreateTaskSuccessfullyWithValidInput() {
			
			TaskRequest request = TaskRequest.builder().title("Task A").organizationId(orgResponse.getPublicId())
				.projectId(projResponse.getPublicId()).description("Do something important").category(TaskCategory.BUG)
				.startDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(5)).priority(TaskPriority.LOW)
				.build();
			
			TaskResponse response = taskService.createNew(request);
			
			Optional<Task> saved = taskRepository.findByIdScoped(response.getPublicId());
			assertTrue(saved.isPresent());
			
			assertNotNull(response);
			assertTrue(StringUtils.isNotBlank(response.getPublicId()));
			assertEquals("Task A", response.getTitle());
			assertEquals("Do something important", response.getDescription());
			assertEquals(orgResponse.getPublicId(), response.getOrganizationId());
		}
		
		@Test
		@DisplayName("Should throw InvalidRequestException if required fields are missing")
		public void shouldFailIfRequiredFieldsAreMissing() {
			TaskRequest request = TaskRequest.builder()
				.organizationId(orgResponse.getPublicId()).projectId(projResponse.getPublicId()).build();
			
			InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> taskService.createNew(request));
			assertTrue(ex.getMessage().contains("must not be null"));
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException if project does not exist")
		public void shouldFailIfProjectDoesNotExist() {
			String randomProjectId = UUID.randomUUID().toString();
			
			TaskRequest request = TaskRequest.builder().title("Task A").organizationId(orgResponse.getPublicId())
				.projectId(randomProjectId).description("Do something important").category(TaskCategory.BUG)
				.startDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(5)).priority(TaskPriority.LOW)
				.build();
			
			EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> taskService.createNew(request));
			assertEquals("Project not found", ex.getMessage());
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException if organization does not exist")
		public void shouldFailIfOrganizationDoesNotExist() {
			String randomOrgId = UUID.randomUUID().toString();
			
			TaskRequest request = TaskRequest.builder().title("Task A").organizationId(randomOrgId)
				.projectId(projResponse.getPublicId()).description("Do something important").category(TaskCategory.BUG)
				.startDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(5)).priority(TaskPriority.LOW)
				.build();
			
			EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> taskService.createNew(request));
			assertEquals("Organization not found", ex.getMessage());
		}
		
		@Test
		@DisplayName("Should associate task correctly with project and organization")
		public void shouldAssociateTaskWithCorrectProjectAndOrganization() {
			
			TaskRequest request = TaskRequest.builder().title("Task A").organizationId(orgResponse.getPublicId())
				.projectId(projResponse.getPublicId()).description("Do something important").category(TaskCategory.BUG)
				.startDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(5)).priority(TaskPriority.LOW)
				.build();
			
			TaskResponse response = taskService.createNew(request);
			assertNotNull(response);
			
			Optional<Task> saved = taskRepository.findByIdScoped(response.getPublicId());
			assertTrue(saved.isPresent());
			
			Task task = saved.get();
			
			assertEquals(orgResponse.getPublicId(), task.getOrganization().getPublicId());
			assertEquals(projResponse.getPublicId(), task.getProject().getPublicId());
		}
		
		@Test
		@DisplayName("Should apply default values when fields are not provided")
		public void shouldSetDefaultValuesOnCreation() {
			TaskRequest request = TaskRequest.builder().title("Task A").organizationId(orgResponse.getPublicId())
					                      .projectId(projResponse.getPublicId()).description("Do something important").category(TaskCategory.BUG)
					                      .startDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(5)).priority(TaskPriority.LOW)
					                      .build();
			
			TaskResponse response = taskService.createNew(request);
			assertNotNull(response);
			
			assertNotNull(response.getStatus());
			assertEquals(TaskStatus.TODO, response.getStatus());
		}
		
		@Test
		public void shouldFailIfUserDoesNotHavePermission() {

		}
	}
	
	@Nested
	@DisplayName("Update Task Tests")
	class UpdateTaskTests {
		
		TaskResponse taskResponse;
		
		@BeforeEach
		public void setUp() {
			TaskRequest taskRequest = TaskRequest.builder().title("Task A").organizationId(orgResponse.getPublicId())
				.projectId(projResponse.getPublicId()).description("Do something important").category(TaskCategory.BUG)
				.startDate(LocalDate.now().plusDays(2)).dueDate(LocalDate.now().plusDays(5))
				.priority(TaskPriority.LOW).build();

			taskResponse = taskService.createNew(taskRequest);
		}
		
		@Test
		@DisplayName("should update task successfully with valid input")
		public void shouldUpdateTaskSuccessfullyWithValidInput() {
			
			TaskUpdateRequest updateRequest = new TaskUpdateRequest(
				"Updated Task A", "Updated description", LocalDate.now().plusDays(10),
				LocalDate.now().plusDays(1), TaskCategory.FEATURE, TaskPriority.HIGH
			);
			
			
			TaskResponse updatedTask = taskService.update(taskResponse.getPublicId(), updateRequest);
			
			assertNotNull(updatedTask);
			
			assertNotEquals(taskResponse.getTitle(), updatedTask.getTitle());
			assertEquals("Updated Task A", updatedTask.getTitle());
			
			assertNotEquals(taskResponse.getDescription(), updatedTask.getDescription());
			assertEquals("Updated description", updatedTask.getDescription());
			
			assertNotEquals(taskResponse.getPriority(), updatedTask.getPriority());
			assertEquals(TaskPriority.HIGH, updatedTask.getPriority());
			
			assertNotEquals(taskResponse.getCategory(), updatedTask.getCategory());
			assertEquals(TaskCategory.FEATURE, updatedTask.getCategory());
			
			assertNotEquals(taskResponse.getStartDate(), updatedTask.getStartDate());
			assertEquals(LocalDate.now().plusDays(1), updatedTask.getStartDate());
			
			assertNotEquals(taskResponse.getDueDate(), updatedTask.getDueDate());
			assertEquals(LocalDate.now().plusDays(10), updatedTask.getDueDate());
		}
		
		@Test
		@DisplayName("should throw EntityNotFoundException if task does not exist")
		public void shouldFailIfTaskDoesNotExist() {
			
			String randomTaskId = UUID.randomUUID().toString();
			
			TaskUpdateRequest updateRequest = new TaskUpdateRequest(
					"Updated Task A", "Updated description", LocalDate.now().plusDays(10),
					LocalDate.now().plusDays(1), TaskCategory.FEATURE, TaskPriority.HIGH
			);
			
			EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
					() -> taskService.update(randomTaskId, updateRequest));
			assertEquals("Task not found", ex.getMessage());
		}
	}
	
	@Nested
	@DisplayName("Assign Member to Task Tests")
	class AssignMemberTests {
		
		TaskResponse taskResponse;
		MemberResponse memberResponse;
		
		@BeforeEach
		void setUp() {
			TaskRequest taskRequest = TaskRequest.builder()
					                          .title("Task A").organizationId(orgResponse.getPublicId())
					                          .projectId(projResponse.getPublicId()).description("Do something important")
					                          .category(TaskCategory.BUG).startDate(LocalDate.now())
					                          .dueDate(LocalDate.now().plusDays(5)).priority(TaskPriority.LOW)
					                          .build();
			taskResponse = taskService.createNew(taskRequest);
			
			Member member = Member.builder()
	                .firstName("Jane").lastName("Doe").email("jane.doe@test.com").password("").role(Role.ORGANIZATION_MEMBER)
	                .organization(organizationRepository.findByIdScoped(orgResponse.getPublicId()).get()).build();
			
			memberResponse = modelMapper.map(userRepository.save(member), MemberResponse.class);
		}
		
		@Test
		@DisplayName("should assign member to task successfully")
		void shouldAssignMemberToTaskSuccessfully() {
			TaskResponse updated = taskService.assignMember(taskResponse.getPublicId(), memberResponse.getPublicId());
			
			assertNotNull(updated.getAssignee());
			assertEquals(memberResponse.getPublicId(), updated.getAssignee().getPublicId());
			assertEquals(memberResponse.getEmail(), updated.getAssignee().getEmail());
		}
		
		@Test
		@DisplayName("should throw EntityNotFoundException if task does not exist")
		void shouldFailIfTaskDoesNotExist() {
			String randomTaskId = UUID.randomUUID().toString();
			
			EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
					() -> taskService.assignMember(randomTaskId, memberResponse.getPublicId()));
			
			assertEquals("Task not found", ex.getMessage());
		}
		
		@Test
		@DisplayName("should throw EntityNotFoundException if member does not exist")
		void shouldFailIfMemberDoesNotExist() {
			String randomMemberId = UUID.randomUUID().toString();
			
			EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
					() -> taskService.assignMember(taskResponse.getPublicId(), randomMemberId));
			
			assertEquals("Member not found", ex.getMessage());
		}
		
		@Test
		@DisplayName("should throw InvalidRequestException if task is already completed")
		void shouldFailIfTaskIsCompleted() {
			// simulate task marked as completed
			Task task = taskRepository.findByIdScoped(taskResponse.getPublicId()).get();
			task.setStatus(TaskStatus.DONE);
			taskRepository.save(task);
			
			InvalidRequestException ex = assertThrows(InvalidRequestException.class,
					() -> taskService.assignMember(task.getPublicId(), memberResponse.getPublicId()));
			
			assertEquals("Task is already completed, you can't assign a member to an already completed task", ex.getMessage());
		}
		
		@Test
		@DisplayName("should throw InvalidRequestException if task is archived")
		void shouldFailIfTaskIsArchived() {
			Task task = taskRepository.findByIdScoped(taskResponse.getPublicId()).get();
			task.setStatus(TaskStatus.ARCHIVED);
			taskRepository.save(task);
			
			InvalidRequestException ex = assertThrows(InvalidRequestException.class,
					() -> taskService.assignMember(task.getPublicId(), memberResponse.getPublicId()));
			
			assertEquals("Task is archived, you can't assign a member to an archived task", ex.getMessage());
		}
		
		@Test
		@DisplayName("should throw InvalidRequestException if member is already assigned")
		void shouldFailIfMemberAlreadyAssigned() {
			taskService.assignMember(taskResponse.getPublicId(), memberResponse.getPublicId());
			
			InvalidRequestException ex = assertThrows(InvalidRequestException.class,
					() -> taskService.assignMember(taskResponse.getPublicId(), memberResponse.getPublicId()));
			
			assertEquals("Member is already assigned to this task", ex.getMessage());
		}
		
		@Test
		@DisplayName("should reassign member if a different member is assigned to task")
		void shouldReassignMemberToTaskSuccessfully() {
			// Member 1:
			taskService.assignMember(taskResponse.getPublicId(), memberResponse.getPublicId());
			
			// Member 2:
			Member member = Member.builder()
				.firstName("John").lastName("Smith").email("john.smith@test.com")
				.organization(organizationRepository.findByIdScoped(orgResponse.getPublicId()).get()).build();
			
			Member secondMember = userRepository.save(member);
			
			TaskResponse updated = taskService.assignMember(taskResponse.getPublicId(), secondMember.getPublicId());
			
			assertNotNull(updated.getAssignee());
			assertEquals(secondMember.getPublicId(), updated.getAssignee().getPublicId());
		}
	}
	
	@Nested
	@DisplayName("Find and Delete Task Tests")
	class FindAndDeleteTaskTests {
		
		TaskResponse createdTask;
		
		@BeforeEach
		void setUp() {
			TaskRequest request = TaskRequest.builder()
					                      .title("Sample Task")
					                      .organizationId(orgResponse.getPublicId())
					                      .projectId(projResponse.getPublicId())
					                      .description("Sample Desc")
					                      .category(TaskCategory.FEATURE)
					                      .startDate(LocalDate.now())
					                      .dueDate(LocalDate.now().plusDays(3))
					                      .priority(TaskPriority.MEDIUM)
					                      .build();
			
			createdTask = taskService.createNew(request);
		}
		
		@Test
		@DisplayName("Should find task by ID if exists")
		void shouldFindTaskById() {
			TaskResponse found = taskService.findById(createdTask.getPublicId());
			
			assertNotNull(found);
			assertEquals(createdTask.getPublicId(), found.getPublicId());
			assertEquals("Sample Task", found.getTitle());
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException if task not found")
		void shouldThrowIfTaskNotFound() {
			String randomId = UUID.randomUUID().toString();
			
			EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
					() -> taskService.findById(randomId));
			
			assertEquals("Task not found", ex.getMessage());
		}
		
		@Test
		@DisplayName("Should return all tasks in the organization")
		void shouldFindAllTasksInTenant() {
			Set<TaskResponse> all = taskService.findAll();
			
			assertNotNull(all);
			assertFalse(all.isEmpty());
			
			assertTrue(all.stream().anyMatch(t -> t.getPublicId().equals(createdTask.getPublicId())));
		}
		
		@Test
		@DisplayName("Should soft delete task by ID")
		void shouldDeleteTaskById() {
			// Confirm task exists
			Optional<Task> beforeDelete = taskRepository.findByIdScoped(createdTask.getPublicId());
			assertTrue(beforeDelete.isPresent());
			
			taskService.deleteById(createdTask.getPublicId());
			
			// After deletion, findByIdScoped should not return it
			Optional<Task> afterDelete = taskRepository.findByIdScoped(createdTask.getPublicId());
			assertFalse(afterDelete.isPresent());
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException if deleting non-existent task")
		void shouldThrowWhenDeletingNonexistentTask() {
			String randomId = UUID.randomUUID().toString();
			
			EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
					() -> taskService.deleteById(randomId));
			
			assertEquals("Task not found", ex.getMessage());
		}
	}
	
}

