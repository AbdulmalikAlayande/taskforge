package app.bola.taskforge.integration;

import app.bola.taskforge.domain.context.TenantContext;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.exception.TaskForgeException;
import app.bola.taskforge.repository.InvitationRepository;
import app.bola.taskforge.security.provider.JwtTokenProvider;
import app.bola.taskforge.service.MemberService;
import app.bola.taskforge.service.OrganizationService;
import app.bola.taskforge.service.dto.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
public class MemberIntegrationTest {

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private MemberService memberService;

	OrganizationResponse orgResponse;
	MemberResponse testMember;
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	@Autowired
	private InvitationRepository invitationRepository;
	
	@BeforeEach
	public void setup() {
		OrganizationRequest orgRequest = OrganizationRequest.builder()
	          .contactEmail("testorgemail@gmail.com").name("Test Inc.").slug("test-inc")
	          .description("A test organization").industry("OTHER").timeZone("Africa/Lagos")
	          .contactPhone("+0123456789").country("Nigeria").logoUrl("https://testinc.org/logo.png")
	          .websiteUrl("https://testinc.org").build();

		orgResponse = organizationService.createNew(orgRequest);
		TenantContext.setCurrentTenant(orgResponse.getPublicId());
		
		InvitationRequest initInvRequest = InvitationRequest.builder()
			.organizationId(orgResponse.getPublicId()).name("Test Admin").email("testadminuser1@gmail.com")
            .role("ORGANIZATION_ADMIN").invitedBy("").build();

		organizationService.inviteMember(initInvRequest);
		
		MemberRequest memberRequest = MemberRequest.builder()
              .organizationId(orgResponse.getPublicId()).firstName("Test Admin").lastName("Member")
              .email("testadminuser1@gmail.com").password("test#admin$password123#").build();

		testMember = memberService.createNew(memberRequest);


	}

	@Nested
	@DisplayName("Accept Invitation Tests")
	class AcceptInvitationTests {
		
		@Test
		@DisplayName("Should accept an invitation successfully, if all checks passed")
		public void shouldAcceptAnInvitationSuccessfully() {
			//Given
			InvitationRequest invRequest = InvitationRequest.builder()
					                               .invitedBy(testMember.getPublicId()).email("newuseremail@gmail.com").role("ORGANIZATION_MEMBER")
					                               .name("New User").organizationId(orgResponse.getPublicId()).build();
			
			InvitationResponse invResponse = organizationService.inviteMember(invRequest);
			
			//When
			String token = invResponse.getInvitationLink().split("=")[1];
			InvitationResponse acceptanceResponse = memberService.acceptInvitation(token);
			assertNotNull(acceptanceResponse);
			assertEquals(acceptanceResponse.getOrganizationId(), orgResponse.getPublicId());
			assertEquals(acceptanceResponse.getEmail(), invRequest.getEmail());
		}
		
		@Test
		@DisplayName("Should throw invalid request exception if token is expired")
		public void shouldFailIfTokenIsExpired() {
			// Given
			String token = jwtTokenProvider.generateToken(Map.of("subject", "email@gmail.com"), 2000);
			await().atMost(Duration.of(5, ChronoUnit.SECONDS))
					.pollDelay(Duration.of(3, ChronoUnit.SECONDS))
					.until(() -> {
						try {
							memberService.acceptInvitation(token);
							return false;
						} catch (InvalidRequestException e) {
							return true;
						}
					});
			// When & Then
			assertThrows(InvalidRequestException.class, () -> memberService.acceptInvitation(token));
		}
		
		@Test
		@DisplayName("Should throw invalid request exception if token is malformed or invalid")
		public void shouldFailIfTokenIsInvalid() {
			// Given
			String token = jwtTokenProvider.generateToken(Map.of("subject", "email@gmail.com"), 5000);
			
			String invalidToken = token.substring(0, token.length() - 6) + "qW6RTy"; // Malformed token
			// When & Then
			assertThrows(InvalidRequestException.class, () -> memberService.acceptInvitation(invalidToken));
		}
		
		@Test
		@DisplayName("Should throw entity not found exception if invitation does not exist")
		public void shouldFailIfInvitationDoesNotExist() {
			// Given
			String nonExistentInvitationToken = jwtTokenProvider.generateToken(Map.of("subject", "nonexistentemail@gmail.com"), 20000);
			
			InvitationRequest invRequest = InvitationRequest.builder()
					                               .invitedBy(testMember.getPublicId())
					                               .email("existentuser@gmail.com")
					                               .role("ORGANIZATION_MEMBER")
					                               .name("Existent User")
					                               .organizationId(orgResponse.getPublicId())
					                               .build();
			
			organizationService.inviteMember(invRequest);
			assertTrue(invitationRepository.findByEmail(invRequest.getEmail()).isPresent());
			
			// When & Then
			EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> memberService.acceptInvitation(nonExistentInvitationToken));
			assertEquals("Invitation does not exist for email: nonexistentemail@gmail.com", exception.getMessage());
		}
		
		@Test
		@DisplayName("Should throw TaskForgeException if invitation has already been accepted")
		public void shouldFailIfInvitationHasAlreadyBeenAccepted() {
			// Given
			InvitationRequest invRequest = InvitationRequest.builder()
					                               .invitedBy(testMember.getPublicId())
					                               .email("alreadyaccepted@example.com")
					                               .role("ORGANIZATION_MEMBER")
					                               .name("Already Accepted")
					                               .organizationId(orgResponse.getPublicId())
					                               .build();
			
			InvitationResponse invResponse = organizationService.inviteMember(invRequest);
			String token = invResponse.getInvitationLink().split("=")[1];
			
			// Accept the invitation first
			memberService.acceptInvitation(token);
			
			// When & Then - Try to accept the invitation again
			assertThrows(TaskForgeException.class, () -> memberService.acceptInvitation(token));
		}
	}

	@Nested
	@DisplayName("Create member tests")
	class CreateNewMemberTests {
		
		InvitationResponse invitationResponse;
		
		@BeforeEach
		public void setup() {
			InvitationRequest request = InvitationRequest.builder()
				.organizationId(orgResponse.getPublicId()).name("New Member").email("newmember@example.com")
                .role("ORGANIZATION_MEMBER").invitedBy(testMember.getPublicId()).build();
			
			invitationResponse = organizationService.inviteMember(request);
		}
		
		@AfterEach
		public void cleanup() {
			invitationRepository.deleteByIdScoped(invitationResponse.getPublicId());
		}
		
	    @Test
	    @DisplayName("Should create a new member successfully, If all checks passed")
	    public void shouldCreateNewMemberSuccessfully(){
	        // Given
	        MemberRequest memberRequest = MemberRequest.builder()
	            .organizationId(orgResponse.getPublicId())
	            .firstName("New")
	            .lastName("Member")
	            .email("newmember@example.com")
	            .password("password123")
	            .build();
	
	        // When
	        MemberResponse response = memberService.createNew(memberRequest);
	
	        // Then
	        assertNotNull(response);
	        assertEquals("newmember@example.com", response.getEmail());
	        assertEquals("New", response.getFirstName());
	        assertEquals("Member", response.getLastName());
	        assertTrue(response.isActive());
	        assertNotNull(response.getPublicId());
	    }
	
	    @Test
	    @DisplayName("Should thrown InvalidRequestException if required fields are missing or fails validation contract")
	    public void shouldFailIfRequiredFieldsAreMissingOrFailsValidationContract() {
	        MemberRequest requestWithMissingEmail = MemberRequest.builder()
	            .organizationId(orgResponse.getPublicId())
	            .firstName("Test")
	            .lastName("Member")
	            .password("password123")
	            .build();
	
	        assertThrows(InvalidRequestException.class, () -> memberService.createNew(requestWithMissingEmail));
	
	        // Test with empty email
	        MemberRequest requestWithEmptyEmail = MemberRequest.builder()
	            .organizationId(orgResponse.getPublicId())
	            .firstName("Test")
	            .lastName("Member")
	            .email("")
	            .password("password123")
	            .build();
	
	        assertThrows(InvalidRequestException.class, () -> memberService.createNew(requestWithEmptyEmail));
	
	        // Test with invalid email format
	        MemberRequest requestWithInvalidEmail = MemberRequest.builder()
	            .organizationId(orgResponse.getPublicId())
	            .firstName("Test")
	            .lastName("Member")
	            .email("invalid-email")
	            .password("password123")
	            .build();
	
	        assertThrows(InvalidRequestException.class, () -> memberService.createNew(requestWithInvalidEmail));
	
	        // Test with missing password
	        MemberRequest requestWithMissingPassword = MemberRequest.builder()
	            .organizationId(orgResponse.getPublicId())
	            .firstName("Test")
	            .lastName("Member")
	            .email("test@example.com")
	            .build();
	
	        assertThrows(InvalidRequestException.class, () -> memberService.createNew(requestWithMissingPassword));
	
	        // Test with missing firstName
	        MemberRequest requestWithMissingFirstName = MemberRequest.builder()
	            .organizationId(orgResponse.getPublicId())
	            .lastName("Member")
	            .email("test@example.com")
	            .password("password123")
	            .build();
	
	        assertThrows(InvalidRequestException.class, () -> memberService.createNew(requestWithMissingFirstName));
	
	        // Test with missing lastName
	        MemberRequest requestWithMissingLastName = MemberRequest.builder()
	            .organizationId(orgResponse.getPublicId())
	            .firstName("Test")
	            .email("test@example.com")
	            .password("password123")
	            .build();
	
	        assertThrows(InvalidRequestException.class, () -> memberService.createNew(requestWithMissingLastName));
	
	        // Test with missing organizationId
	        MemberRequest requestWithMissingOrgId = MemberRequest.builder()
	            .firstName("Test")
	            .lastName("Member")
	            .email("test@example.com")
	            .password("password123")
	            .build();
	
	        assertThrows(InvalidRequestException.class, () -> memberService.createNew(requestWithMissingOrgId));
	    }
	
	    @Test
	    @DisplayName("Should throw TaskForgeException if member exists")
	    public void shouldFailIfMemberExists() {
	        // Given
	        // Use the email of the testMember that's already created in the setup method
	        MemberRequest memberRequest = MemberRequest.builder()
	            .organizationId(orgResponse.getPublicId())
	            .firstName("Duplicate")
	            .lastName("Member")
	            .email(testMember.getEmail()) // Use the same email as the existing member
	            .password("password123")
	            .build();
	
	        // When & Then
	        assertThrows(TaskForgeException.class, () -> memberService.createNew(memberRequest));
	    }
	
	    @Test
	    @DisplayName("Should throw EntityNotFoundException If Organization does not exist")
	    public void shouldFailIfOrganizationDoesNotExist() {
	        // Given
	        String nonExistentOrgId = "non-existent-org-" + java.util.UUID.randomUUID().toString();
	
	        MemberRequest memberRequest = MemberRequest.builder()
	            .organizationId(nonExistentOrgId)
	            .firstName("New")
	            .lastName("Member")
	            .email("newmember@example.com")
	            .password("password123")
	            .build();
	
	        // When & Then
	        assertThrows(EntityNotFoundException.class, () -> memberService.createNew(memberRequest));
	    }
	
	    @Test
	    @DisplayName("Should throw InvalidRequestException if there is no valid and accepted invitation for the member")
	    public void shouldFailIfNoValidAndAcceptedInvitationExistsForMember() {
	        // Given
	        // Create a member request with an email that doesn't have an invitation
	        String emailWithoutInvitation = "noinvitation@example.com";
	
	        MemberRequest memberRequest = MemberRequest.builder()
	            .organizationId(orgResponse.getPublicId())
	            .firstName("No")
	            .lastName("Invitation")
	            .email(emailWithoutInvitation)
	            .password("password123")
	            .build();
	
	        // When & Then
	        assertThrows(InvalidRequestException.class, () -> memberService.createNew(memberRequest));
	    }
	}
}
