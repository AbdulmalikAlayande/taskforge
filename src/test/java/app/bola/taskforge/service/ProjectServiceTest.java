package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.DateRange;
import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.entity.Project;
import app.bola.taskforge.domain.entity.User;
import app.bola.taskforge.domain.enums.ProjectCategory;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.service.dto.OrganizationResponse;
import app.bola.taskforge.service.dto.ProjectRequest;
import app.bola.taskforge.service.dto.ProjectResponse;
import app.bola.taskforge.service.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(value = MockitoExtension.class)
class ProjectServiceTest {
	
	@Mock
	private ModelMapper modelMapper;
	@Mock
	private UserRepository userRepository;
	@Mock
	private OrganizationRepository organizationRepository;
	@InjectMocks
	private TaskForgeProjectService taskForgeProjectService;
	
	@Nested
	@DisplayName("Create New Project Tests")
	class CreateNewProjectTests {
		
		@Test
		@DisplayName("""
		Should successfully create a new project when all validations are passed,
		organization exists and all members exist
		""")
		public void shouldCreateNewProjectSuccessfully() {
			//Given
			ProjectRequest projectRequest = ProjectRequest.builder()
					.name("New  Project")
                    .description("This is a new project")
                    .category("SOFTWARE")
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(31))
                    .memberIds(List.of("member-id-1", "member-id-2", "member-id-3"))
                    .organizationId("organization-id-12345")
                    .build();
			
			when(modelMapper.map(projectRequest, Project.class)).thenReturn(Project.builder()
					.name("New  Project")
					.description("This is a new project")
					.category(ProjectCategory.valueOf("SOFTWARE"))
					.dateRange(new DateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(30)))
					.members(Set.of())
					.build());
			
			when(organizationRepository.findByIdScoped("organization-id-12345"))
				.thenReturn(Optional.of(Organization.builder().id("organization-id-12345").name("Test Organization").build()));
			
			when(userRepository.findAllByIdScoped(List.of("member-id-1", "member-id-2", "member-id-3")))
				.thenReturn(List.of(
					User.builder().id("member-id-1").email("").firstName("John").lastName("Doe").build(),
					User.builder().id("member-id-2").email("").firstName("Jane").lastName("Doe").build(),
					User.builder().id("member-id-3").email("").firstName("Jim").lastName("Beam").build()
				));
			when(organizationRepository.findByIdScoped("organization-id-12345"))
				.thenReturn(Optional.of(Organization.builder().id("organization-id-12345").name("Test Organization").build()));
			
			when(userRepository.findAllByIdScoped(List.of("member-id-1", "member-id-2", "member-id-3")))
				.thenReturn(List.of(
					User.builder().publicId("member-id-1").email("").firstName("John").lastName("Doe").build(),
					User.builder().publicId("member-id-2").email("").firstName("Jane").lastName("Doe").build(),
					User.builder().publicId("member-id-3").email("").firstName("Jim").lastName("Beam").build()
				));
			when(modelMapper.map(any(Project.class), eq(ProjectResponse.class))).thenReturn(ProjectResponse.builder()
					.name("New  Project").description("This is a new project").category(ProjectCategory.SOFTWARE.name())
					.startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(31))
					.members(Set.of(
						UserResponse.builder().publicId("member-id-1").firstName("John").lastName("Doe").build(),
						UserResponse.builder().publicId("member-id-2").firstName("Jane").lastName("Doe").build(),
						UserResponse.builder().publicId("member-id-3").firstName("Jim").lastName("Beam").build()
					))
					.organization(OrganizationResponse.builder().publicId("organization-id-12345").name("Test Organization").build())
					.build());
			
			//When
			ProjectResponse projectResponse = taskForgeProjectService.createNew(projectRequest);
			
			//Then
			assertNotNull(projectResponse);
			assertEquals("New  Project", projectResponse.getName());
			assertEquals("This is a new project", projectResponse.getDescription());
			assertThat(projectResponse).hasAllNullFieldsOrPropertiesExcept(
				"name", "description", "category", "startDate", "endDate", "members", "organization", "archived", "publicId"
			);
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException when organization does not exist")
		public void shouldThrowEntityNotFoundExceptionWhenOrganizationDoesNotExist() {

		}
		
		@Test
		@DisplayName("Should throw InvalidRequestException when project category is invalid")
		public void shouldThrowInvalidRequestExceptionWhenProjectCategoryIsInvalid() {

		}
		
		@Test
		@DisplayName("Should throw IllegalArgumentException when start date is after end date")
		public void shouldThrowIllegalArgumentExceptionWhenStartDateIsAfterEndDate() {

		}
	}
	
	@Nested
	@DisplayName("Add Member To Project Tests")
	class AddMemberToProjectTests {
		
		@Test
		@DisplayName("Should add member to a project successfully when project and member exists")
		public void shouldAddMemberToProjectSuccessfully() {
			// Given
			String projectId = "project-id-12345";
			String memberId = "member-id-12345";
			
			Project project = Project.builder().publicId(projectId).name("Test Project").build();
			User member = User.builder().id(memberId).firstName("John").lastName("Doe").build();
			
			when(modelMapper.map(any(Project.class), eq(ProjectResponse.class))).thenReturn(
				ProjectResponse.builder().publicId(projectId).name("Test Project").build()
			);
			when(userRepository.findByIdScoped(memberId)).thenReturn(Optional.of(member));
			when(taskForgeProjectService.addMember(projectId, memberId)).thenReturn(
				ProjectResponse.builder().publicId(projectId).name("Test Project").members(Set.of(
					UserResponse.builder().publicId(memberId).firstName("John").lastName("Doe").build()
				)).build()
			);
			
			// When
			ProjectResponse response = taskForgeProjectService.addMember(projectId, memberId);
			
			// Then
			assertNotNull(response);
			assertEquals(projectId, response.getPublicId());
			assertEquals("Test Project", response.getName());
			assertTrue(response.getMembers().stream().anyMatch(m -> m.getPublicId().equals(memberId)));
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException when project does not exist")
		public void shouldThrowEntityNotFoundExceptionWhenProjectDoesNotExist() {
			// Given
			String projectId = "non-existent-project-id";
			String memberId = "member-id-12345";
			
			when(userRepository.findByIdScoped(memberId)).thenReturn(Optional.of(
				User.builder().id(memberId).firstName("John").lastName("Doe").build()
			));
			when(taskForgeProjectService.addMember(projectId, memberId)).thenThrow(
				new EntityNotFoundException("Project not found with id: " + projectId)
			);
			
			// When & Then
			assertThrows(EntityNotFoundException.class, () -> taskForgeProjectService.addMember(projectId, memberId));
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException when member does not exist")
		public void shouldThrowEntityNotFoundExceptionWhenMemberDoesNotExist() {
			// Given
			String projectId = "project-id-12345";
			String memberId = "non-existent-member-id";
			
			when(userRepository.findByIdScoped(memberId)).thenReturn(Optional.empty());
			
			// When & Then
			assertThrows(EntityNotFoundException.class, () -> taskForgeProjectService.addMember(projectId, memberId));
		}
		
		@Test
		@DisplayName("Should handle thrown TaskForgeException gracefully when member is already in project")
		public void shouldHandleThrownExceptionGracefullyWhenMemberAlreadyInProject() {
			// Given
			String projectId = "project-id-12345";
			String memberId = "member-id-12345";
			
			Project project = Project.builder().publicId(projectId).name("Test Project").build();
			User member = User.builder().id(memberId).firstName("John").lastName("Doe").build();
			
			when(modelMapper.map(any(Project.class), eq(ProjectResponse.class))).thenReturn(
				ProjectResponse.builder().publicId(projectId).name("Test Project").build()
			);
			when(userRepository.findByIdScoped(memberId)).thenReturn(Optional.of(member));
			when(taskForgeProjectService.addMember(projectId, memberId)).thenReturn(
				ProjectResponse.builder().publicId(projectId).name("Test Project").members(Set.of(
					UserResponse.builder().publicId(memberId).firstName("John").lastName("Doe").build()
				)).build()
			);
			
			// When
			ProjectResponse response = taskForgeProjectService.addMember(projectId, memberId);
			
			// Then
			assertNotNull(response);
			assertEquals(projectId, response.getPublicId());
			assertEquals("Test Project", response.getName());
			assertTrue(response.getMembers().stream().anyMatch(m -> m.getPublicId().equals(memberId)));
		}
		
		@Test
		@DisplayName("Should throw InvalidRequestException when member belongs to a different organization")
		public void shouldThrowInvalidRequestExceptionWhenMemberBelongsToDifferentOrganization() {
			// Given
			String projectId = "project-id-12345";
			String memberId = "member-id-12345";
			
			when(userRepository.findByIdScoped(memberId)).thenReturn(Optional.of(
				User.builder().id(memberId).firstName("John").lastName("Doe").build()
			));
			when(taskForgeProjectService.addMember(projectId, memberId)).thenThrow(
				new EntityNotFoundException("Member does not belong to the organization of the project")
			);
			
			// When & Then
			assertThrows(EntityNotFoundException.class, () -> taskForgeProjectService.addMember(projectId, memberId));
		}
	}
}