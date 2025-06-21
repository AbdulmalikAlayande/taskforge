package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.DateRange;
import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.entity.Project;
import app.bola.taskforge.domain.entity.Task;
import app.bola.taskforge.domain.enums.TaskCategory;
import app.bola.taskforge.domain.enums.TaskPriority;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.exception.TaskForgeException;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.repository.ProjectRepository;
import app.bola.taskforge.repository.TaskRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.service.dto.TaskRequest;
import app.bola.taskforge.service.dto.TaskResponse;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
	
	
	@Nested
	@DisplayName("Create New Task Tests")
	public class CreateNewTaskTests {
		
		@Test
		@DisplayName("should create task successfully given valid request and all checks passed")
		public void shouldCreateTaskSuccessfullyWhenAndAllChecksPassed() {
			String organizationId = UUID.randomUUID().toString();
			Organization organization = Organization.builder()
				.publicId(organizationId).name("Mock Organization").slug("mock-org").description("A mock Org")
                .contactPhone("+2347023456789").contactEmail("mockorgemail@gmail.com").industry("Technology").build();
			
			String projectId = UUID.randomUUID().toString();
			Project project = Project.builder().name("Mock Project").publicId(projectId)
				.dateRange(new DateRange(LocalDate.now().plusDays(2), LocalDate.now().plusDays(5)))
				.build();
			
			TaskRequest request = TaskRequest.builder()
				.title("Mock Task").description("Mock Description").projectId(projectId).organizationId(organizationId)
				.dueDate(LocalDate.now().plusDays(3)).startDate(LocalDate.now().plusDays(1))
				.priority(TaskPriority.LOW).category(TaskCategory.FEATURE).build();
			
			Task task = Task.builder()
				.title("Mock Task").description("Mock Description").project(project).organization(organization)
				.startDate(LocalDate.now().plusDays(1)).dueDate(LocalDate.now().plusDays(3))
	            .priority(TaskPriority.LOW).category(TaskCategory.FEATURE).build();
			
			when(modelMapper.map(request, Task.class)).thenReturn(task);
			when(validator.validate(request)).thenReturn(Set.of());
			when(modelMapper.map(task, TaskResponse.class)).thenReturn(new TaskResponse());
			when(organizationRepository.findByIdScoped(any())).thenReturn(Optional.of(organization));
			when(projectRepository.findByIdScoped(projectId)).thenReturn(Optional.of(project));
			when(taskRepository.save(task)).thenReturn(task);
			when(modelMapper.map(task, TaskResponse.class)).thenReturn(TaskResponse.builder().title("Mock Task").build());
			
			TaskResponse response = taskService.createNew(request);

			assertNotNull(response);
			assertEquals("Mock Task", response.getTitle());
			verify(taskRepository).save(any(Task.class));
		}
		
		@Test
		@DisplayName("Should throw TaskForgeException when the request object is null")
		public void shouldFailWhenRequestIsNull() {
			Exception exception = assertThrows(TaskForgeException.class, () -> taskService.createNew(null));
			assertEquals("Task request cannot be null", exception.getMessage());
		}
		
		@Test
		@DisplayName("should throw InvalidRequestException when required fields are missing")
		public void shouldFailWhenMissingRequiredFields() {
			
			TaskRequest request = TaskRequest.builder()
				.title("Mock Task").description("Mock Description").build();
			
			doThrow(new InvalidRequestException("")).when(validator.validate(request));
			
			InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> taskService.createNew(request));
			assertEquals("Required fields are missing", exception.getMessage());
		}
		
		@Test
		@DisplayName("should throw InvalidRequestException when due date is invalid or before start date")
		public void shouldFailWhenDueDateIsInvalidOrDueDateIsBeforeStartDate() {
		
		}
		
		@Test
		@DisplayName("should throw EntityNotFoundException when the project does not exist")
		public void shouldFailGivenNonExistentProjectId() { }
		
		@Test
		void shouldThrowException_givenNonExistentMemberIds() { }
	}
	
	
}
