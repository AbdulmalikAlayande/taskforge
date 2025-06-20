package app.bola.taskforge.integration;

import app.bola.taskforge.domain.context.TenantContext;
import app.bola.taskforge.domain.enums.ProjectCategory;
import app.bola.taskforge.repository.UserRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
	@Autowired
	private UserRepository userRepository;
	
	@Nested
	class CreateProjectTests {
		
		List<String> testMemberIds;
		OrganizationResponse testOrg;
		
		@BeforeEach
		public void setup() {
			testMemberIds = new ArrayList<>();
			OrganizationRequest orgRequest = OrganizationRequest.builder()
                .name("Test Organization").slug("test-org-123").industry("Technology").country("Nigeria")
			    .timeZone("Africa/Lagos").contactEmail("contact@testorg.com").contactPhone("+2348012345678")
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
		
		@Test void shouldCreateProjectWithEmptyMemberIds() {
		
			
		}
		@Test void shouldFailWhenOrganizationNotFound() {}
		@Test void shouldLogMissingMembersWhenSomeMemberIdsInvalid() {}
		@Test void shouldHandleAllMemberIdsInvalid() {}
		@Test void shouldFailValidationForEmptyNameOrMissingDates() {}
	}
	
	@Nested
	class AddMemberToProjectTests {
		@Test void shouldAddValidMemberToProject() {}
		@Test void shouldFailWhenProjectIdInvalid() {}
		@Test void shouldFailWhenMemberIdInvalid() {}
		@Test void shouldFailWhenMemberAlreadyInProject() {}
	}
	
	@Nested
	class RemoveMemberFromProjectTests {
		@Test void shouldRemoveExistingMemberFromProject() {}
		@Test void shouldFailWhenProjectNotFound() {}
		@Test void shouldFailWhenMemberNotFound() {}
		@Test void shouldNotFailWhenMemberNotInProject() {}
	}
	
	@Nested
	class ChangeProjectStatusTests {
		@Test
		void shouldChangeStatusToCompletedOrPaused() {}
		@Test void shouldFailWhenProjectNotFound() {}
		@Test void shouldFailForInvalidStatusString() {}
		@Test void shouldNotChangeStatusForEmptyOrNullInput() {}
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