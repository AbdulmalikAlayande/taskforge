package app.bola.taskforge.integration;

import app.bola.taskforge.domain.context.TenantContext;
import app.bola.taskforge.domain.enums.ProjectCategory;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.service.MemberService;
import app.bola.taskforge.service.OrganizationService;
import app.bola.taskforge.service.ProjectService;
import app.bola.taskforge.service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ProjectIntegrationTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ProjectIntegrationTest.class);
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private ProjectService projectService;
	
	List<String> testMemberIds;
	OrganizationResponse testOrg;
	
	@BeforeEach
	public void setup() {
		testMemberIds = new ArrayList<>();
		OrganizationRequest orgRequest = OrganizationRequest.builder()
				                                 .name("Test Organization").slug("test-org-123").industry("Technology").country("Nigeria")
				                                 .timeZone("Africa/Lagos").email("contact@testorg.com").phone("+2348012345678")
				                                 .description("A test organization").websiteUrl("https://testorg.com").logoUrl("https://testorg.com/logo.png")
				                                 .build();
		
		testOrg = organizationService.createNew(orgRequest);
		TenantContext.setCurrentTenant(testOrg.getPublicId());
		
		for (int index = 0; index < 3; index++) {
			String email = "email" + index + System.currentTimeMillis() + "@gmail.com";
			InvitationResponse invitationResponse = organizationService.inviteMember(
					InvitationRequest.builder()
							.email(email).name("Test"+index).role("ORGANIZATION_ADMIN")
							.organizationId(testOrg.getPublicId()).build()
			);
			
			String invitationToken = invitationResponse.getInvitationLink().split("=")[1];
			memberService.acceptInvitation(invitationToken);
			
			MemberResponse memberResponse = memberService.createNew(
					MemberRequest.builder()
							.password("password-" + index+System.currentTimeMillis())
							.email(email)
							.firstName("Test"+index)
							.lastName("Member"+index)
							.organizationId(testOrg.getPublicId())
							.build()
			);
			logger.info("member response {} {}", index+1, memberResponse);
			testMemberIds.add(memberResponse.getPublicId());
		}
	}
	
	
	@Nested
	class CreateProjectTests {
		
		@Test
		@DisplayName("Should create a project successfully if the provided data is valid and all checks passed")
		public void shouldCreateProjectWithValidData() {
			//Given: We have a project with valid data
			ProjectRequest request = ProjectRequest.builder()
                 .name("Carabao").description("A carabao cup match sign up site").category(ProjectCategory.OTHER)
                 .endDate(LocalDate.now().plusDays(20)).startDate(LocalDate.now().plusDays(1))
                 .memberIds(testMemberIds).organizationId(testOrg.getPublicId())
                 .build();
			
			//When: We create a new project
			ProjectResponse response = projectService.createNew(request);
			
			//Then: Assert that the response is not null, and has a publicId
			assertNotNull(response);
			assertNotNull(response.getPublicId());
		}
		
		@Test
		@DisplayName("Should create a project, even if no members are added to the project")
		public void shouldCreateProjectWithEmptyMemberIds() {
			// Given: A project request with empty member ID list
			ProjectRequest request = ProjectRequest.builder()
                 .name("Task Scheduler").description("An application that helps with task and even scheduling")
                 .endDate(LocalDate.now().plusDays(20)).startDate(LocalDate.now().plusDays(1))
                 .category(ProjectCategory.SOFTWARE).memberIds(new ArrayList<>()).organizationId(testOrg.getPublicId())
                 .build();
			
			// When:  I try to create the project
			// Then: The project should be created successfully
			ProjectResponse response = projectService.createNew(request);
			
			//Assert: The response is not null, and the response has a public Id;
			assertNotNull(response);
			assertThat(response.getPublicId()).isNotBlank();
			List<String> emptyMembersList = response.getMembers().stream().map(MemberResponse::getPublicId).toList();
			assertEquals(emptyMembersList, request.getMemberIds());
		}
		
		@Test
		@DisplayName("Should log missing members, when some/all member Ids are invalid and the members could not be found")
		public void shouldLogMissingMembersWhenSomeMemberIdsInvalid() {
		
		}
		
		@Test
		@DisplayName("Should handle all exceptions thrown when all member Ids are invalid")
		public void shouldHandleAllMemberIdsInvalid() {
			// Given: A project request with three non-existent member IDs
			List<String> nonExistentMemberIDs = List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
			ProjectRequest request = ProjectRequest.builder()
				.name("Borrow Padi").description("A loan application")
				.endDate(LocalDate.now().plusDays(20)).startDate(LocalDate.now().plusDays(1))
				.category(ProjectCategory.FINANCE).memberIds(nonExistentMemberIDs).organizationId(testOrg.getPublicId())
				.build();
			
			// When: I try to create a project
			// Then: The project should be created successfully and all exceptions thrown should be handled successfully
			ProjectResponse response = assertDoesNotThrow(() -> projectService.createNew(request));
			
			//Assert: The response is not null
			assertNotNull(response);
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException, when organization is not found")
		public void shouldFailWhenOrganizationNotFound() {
			// Given: A project request with a non-existent organization ID.
			String nonExistentOrgId = UUID.randomUUID().toString();
			ProjectRequest request = ProjectRequest.builder()
                 .name("Carabao").description("A carabao cup match sign up site").category(ProjectCategory.OTHER)
                 .endDate(LocalDate.now().plusDays(20)).startDate(LocalDate.now().plusDays(1))
                 .memberIds(testMemberIds).organizationId(nonExistentOrgId)
                 .build();
			
			// When: I try to create a project
			// Then: It should fail by throwing an exception
			EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->projectService.createNew(request));
			
			//Assert: The exception is not null
			assertNotNull(exception);
			assertEquals("Organization not found with id: " + nonExistentOrgId, exception.getMessage());
		}
		
		
		@Test void shouldFailValidationForEmptyNameOrMissingDates() {
			
			// Given: A project request with empty names and missing dates
			ProjectRequest request = ProjectRequest.builder()
                 .name("").description("An empty application")
                 .category(ProjectCategory.SOFTWARE).memberIds(new ArrayList<>()).organizationId(testOrg.getPublicId())
                 .build();
			
			// When: I try to create a project
			// Then: An exception should be thrown because of failed validation
			InvalidRequestException exception = assertThrowsExactly(InvalidRequestException.class, () -> projectService.createNew(request));
			
			// Assert: The exception is not null
			assertNotNull(exception);
			System.out.println(exception.getMessage());
			assertTrue(exception.getMessage().contains("name: must not be blank"));
		}
	}
	
	@Nested
	class AddMemberToProjectTests {
		
		ProjectRequest request;
		ProjectResponse projectResponse;
		
		@BeforeEach
		public void setUp() {
			
			request = ProjectRequest.builder()
				.name("Library 2XX").description("An always successful response library").category(ProjectCategory.SOFTWARE)
				.organizationId(testOrg.getPublicId()).memberIds(testMemberIds)
				.endDate(LocalDate.now().plusDays(20)).startDate(LocalDate.now().plusDays(1))
				.build();
			
			projectResponse = projectService.createNew(request);
		}
		
		private MemberResponse processMemberData() {
			InvitationResponse invResponse = organizationService.inviteMember(InvitationRequest.builder().name("Test Admin Member")
				.organizationId(testOrg.getPublicId()).role("ORGANIZATION_ADMIN").email("testadminmember@gmail.com").build());
			
			memberService.acceptInvitation(invResponse.getInvitationLink().split("=")[1]);
			
			return memberService.createNew(MemberRequest.builder().email("testadminmember@gmail.com").password("test-admin-member@#123")
				.firstName("Test Admin").lastName("Member").organizationId(testOrg.getPublicId()).build());
		}
		
		@Test
		@DisplayName("Should add a valid non-existing(in project) member to a valid project successfully")
		public void shouldAddValidMemberToProject() {
			// Given: A valid project and a member
			MemberResponse member = processMemberData();
			
			//When: I try to add a new non-existing(in project) member to the project
			//Then: The member is added to the project successfully
			ProjectResponse newResponse = projectService.addMember(projectResponse.getPublicId(), member.getPublicId());
			
			//Assert: newResponse is not null, newResponse has one more member
			assertNotNull(newResponse);
			assertNotEquals(newResponse.getMembers().size(), projectResponse.getMembers().size());
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException when an invalid/non-existent project ID is provided")
		public void shouldFailWhenProjectIdInvalid() {
			
			// Given: A non-existent project ID and A valid member
			String nonExistentProjectID = UUID.randomUUID().toString();
			MemberResponse member = processMemberData();
			
			// When: I try to create a project
			// Then: The test should fail by throwing an exception
			EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
					() -> projectService.addMember(nonExistentProjectID, member.getPublicId()));
			
			// Assert: exception is not null
			assertNotNull(exception);
			assertEquals("Project not found with id: " + nonExistentProjectID, exception.getMessage());
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException when an invalid/non-existent member ID is provided")
		public void shouldFailWhenMemberIdInvalid() {
			
			// Given: A non-existent member ID and A valid project ID
			String nonExistentMemberID = UUID.randomUUID().toString();
			
			// When: I try to create a project
			// Then: An exception thrown
			EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
					() -> projectService.addMember(projectResponse.getPublicId(),nonExistentMemberID));
			
			// Assert: exception is not null
			assertNotNull(exception);
			assertEquals("Member not found with id: " + nonExistentMemberID, exception.getMessage());
		}
		
		@Test
		@DisplayName("Should throw InvalidRequestException when member exists in project")
		public void shouldFailWhenMemberAlreadyInProject() {
			// Given: A valid project and a member
			MemberResponse member = processMemberData();
			
			// When: I try to add a member more than once
			// Then: An exception thrown
			InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
				projectService.addMember(projectResponse.getPublicId(), member.getPublicId());
				projectService.addMember(projectResponse.getPublicId(), member.getPublicId());
			});
			
			// Assert: exception is not null
			assertNotNull(exception);
			assertEquals("Member already exists in the project", exception.getMessage());
		}
	}
	
	@Nested
	class RemoveMemberFromProjectTests {
		
		ProjectResponse projectResponse;
		
		@BeforeEach
		public void setUp() {
			
			ProjectRequest request = ProjectRequest.builder()
				.name("Library 2XX").description("An always successful response library").category(ProjectCategory.SOFTWARE)
				.organizationId(testOrg.getPublicId()).memberIds(testMemberIds)
				.endDate(LocalDate.now().plusDays(20)).startDate(LocalDate.now().plusDays(1))
				.build();

			projectResponse = projectService.createNew(request);
		}
		
		private MemberResponse processMemberData() {
			InvitationResponse invResponse = organizationService.inviteMember(InvitationRequest.builder().name("Test Admin Member")
				.organizationId(testOrg.getPublicId()).role("ORGANIZATION_ADMIN").email("testadminmember2@gmail.com").build());
			
			memberService.acceptInvitation(invResponse.getInvitationLink().split("=")[1]);
			
			return memberService.createNew(MemberRequest.builder().email("testadminmember2@gmail.com").password("test-admin-member2@#123")
				.firstName("Test Admin").lastName("Member2").organizationId(testOrg.getPublicId()).build());
		}
		
		@Test
		@DisplayName("Should remove an existing member from a valid project successfully")
		public void shouldRemoveExistingMemberFromProject() {
			// Given: A valid project and a member
			MemberResponse member = processMemberData();
			
			// When: I add the member to the project
			ProjectResponse initialResponse = projectService.addMember(projectResponse.getPublicId(), member.getPublicId());
			assertEquals(initialResponse.getMembers().size(), BigInteger.valueOf(4).intValue());
			
			// When: I try to remove an existing member from the project
			// Then: The member is removed from the project successfully
			ProjectResponse finalResponse = projectService.removeMember(projectResponse.getPublicId(), member.getPublicId());
			
			// Assert: response is not null, response has one less member
			assertNotNull(finalResponse);
			assertEquals(finalResponse.getMembers().size(), BigInteger.valueOf(3).intValue());
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException when an invalid/non-existent project ID is provided")
		public void shouldFailWhenProjectNotFound() {
			// Given: A non-existent project ID and a valid member
			String nonExistentProjectID = UUID.randomUUID().toString();
			MemberResponse member = processMemberData();
			
			// When: I try to remove a member from a non-existent project
			// Then: An exception is thrown
			EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
					() -> projectService.removeMember(nonExistentProjectID, member.getPublicId()));
			
			// Assert: exception is not null
			assertNotNull(exception);
			assertEquals("Project not found with id: " + nonExistentProjectID, exception.getMessage());
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException when an invalid/non-existent member ID is provided")
		public void shouldFailWhenMemberNotFound() {
			// Given: A non-existent member ID and a valid project ID
			String nonExistentMemberID = UUID.randomUUID().toString();
			
			// When: I try to remove a member from a project
			// Then: An exception is thrown
			EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
					() -> projectService.removeMember(projectResponse.getPublicId(), nonExistentMemberID));
			
			// Assert: exception is not null
			assertNotNull(exception);
			assertEquals("Member not found with id: " + nonExistentMemberID, exception.getMessage());
		}
		
		@Test
		@DisplayName("Should not fail when the member is not in the project")
		public void shouldNotFailWhenMemberNotInProject() {
			
			// Given: A valid project and a member that is not in the project
			InvitationResponse invResponse = organizationService.inviteMember(InvitationRequest.builder().name("Test Admin Member3")
				.organizationId(testOrg.getPublicId()).role("ORGANIZATION_ADMIN").email("testadminmember3@gmail.com").build());
			
			memberService.acceptInvitation(invResponse.getInvitationLink().split("=")[1]);
			
			MemberResponse member = memberService.createNew(MemberRequest.builder().email("testadminmember3@gmail.com").password("test-admin-member3@#123")
					                                                .firstName("Test Admin").lastName("Member3").organizationId(testOrg.getPublicId()).build());
			
			// When: I try to remove a member that is not in the project
			// Then: The operation should complete without throwing an exception
			ProjectResponse response = assertDoesNotThrow(() -> projectService.removeMember(projectResponse.getPublicId(), member.getPublicId()));
			
			// Assert: response is not null, response has the same members as before
			assertNotNull(response);
			assertEquals(response.getMembers().size(), projectResponse.getMembers().size());
		}
	}
	
	@Nested
	class ChangeProjectStatusTests {
		
		ProjectResponse projectResponse;
		
		@BeforeEach
		public void setUp() {
			
			ProjectRequest request = ProjectRequest.builder()
				.name("Library 2XX").description("An always successful response library").category(ProjectCategory.SOFTWARE)
				.organizationId(testOrg.getPublicId()).memberIds(testMemberIds)
				.endDate(LocalDate.now().plusDays(20)).startDate(LocalDate.now().plusDays(1))
				.build();
			
			projectResponse = projectService.createNew(request);
		}
		
		@Test
		@DisplayName("Should change project status to COMPLETED or PAUSED successfully")
		public void shouldChangeStatusToCompletedOrPaused() {
			// Given: A valid project and a valid status
			String projectId = projectResponse.getPublicId();
			
			// When: I try to change the status to COMPLETED
			ProjectResponse completedResponse = projectService.changeStatus(projectId, "COMPLETED");
			
			// Then: The status should be changed successfully
			assertNotNull(completedResponse);
			assertEquals("COMPLETED", completedResponse.getStatus().name());
			
			// When: I try to change the status to PAUSED
			ProjectResponse pausedResponse = projectService.changeStatus(projectId, "PAUSED");
			
			// Then: The status should be changed successfully
			assertNotNull(pausedResponse);
			assertEquals("PAUSED", pausedResponse.getStatus().name());
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException when an invalid/non-existent project ID is provided")
		public void shouldFailWhenProjectNotFound() {
			
			// Given: A non-existent project ID
			String nonExistentProjectID = UUID.randomUUID().toString();
			
			// When: I try to change the status of a non-existent project
			// Then: An exception is thrown
			EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
					() -> projectService.changeStatus(nonExistentProjectID, "COMPLETED"));
			
			// Assert: exception is not null
			assertNotNull(exception);
			assertEquals("Project not found with id: " + nonExistentProjectID, exception.getMessage());
		}
		
		@Test
		@DisplayName("Should throw InvalidRequestException when an invalid status string is provided")
		public void shouldFailForInvalidStatusString() {
			
			// Given: A valid project ID and an invalid status string
			String projectId = projectResponse.getPublicId();
			String invalidStatus = "INVALID_STATUS";
			
			// When: I try to change the status to an invalid status
			// Then: An exception is thrown
			InvalidRequestException exception = assertThrows(InvalidRequestException.class,
					() -> projectService.changeStatus(projectId, invalidStatus));
			
			// Assert: exception is not null
			assertNotNull(exception);
			assertEquals("Project status " + "'" + invalidStatus + "'" + " is invalid", exception.getMessage());
		}
		
		@Test
		@DisplayName("Should not change status for empty or null input")
		public void shouldNotChangeStatusForEmptyOrNullInput() {
			
			// Given: A valid project ID and an empty status string
			String projectId = projectResponse.getPublicId();
			
			// When: I try to change the status with an empty string
			ProjectResponse response = projectService.changeStatus(projectId, "");
			assertNotEquals("", response.getStatus().name());
			
			// When: I try to change the status with a null value
			NullPointerException exception = assertThrows(NullPointerException.class,
					() -> projectService.changeStatus(projectId, null));
			
			// Assert: exception is not null
			assertNotNull(exception);
			assertEquals("status is marked non-null but is null", exception.getMessage());
		}
	}
	
	@Nested
	class UpdateProjectTests {
		@Test void shouldUpdateProjectNameOrDescription() {}
		@Test void shouldUpdateWithValidNewDateRange() {}
		@Test void shouldUpdateCategory() {}
		@Test void shouldFailWhenProjectNotFound() {}
		@Test void shouldFailForInvalidDateRange() {}
		@Test void shouldFailForPastStartDate() {}
		@Test void shouldFailForInvalidCategoryEnum() {}
	}
	
	@Nested
	class GetAllByOrganizationIdTests {
		@Test void shouldGetAllProjectsByValidOrgId() {}
		@Test void shouldReturnEmptyListIfNoProjects() {}
		@Test void shouldFailWhenOrgNotFound() {}
	}
}