package app.bola.taskforge.integration;

import app.bola.taskforge.domain.context.TenantContext;
import app.bola.taskforge.domain.entity.Task;
import app.bola.taskforge.domain.enums.ProjectCategory;
import app.bola.taskforge.domain.enums.TaskCategory;
import app.bola.taskforge.domain.enums.TaskPriority;
import app.bola.taskforge.domain.enums.TaskStatus;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.repository.TaskRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
	
	
	@Nested
	@DisplayName("Create New Task Tests")
	class CreateNewTaskTests {
		
		OrganizationResponse orgResponse;
		ProjectResponse projResponse;
		
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
}

