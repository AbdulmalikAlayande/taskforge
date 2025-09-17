package app.bola.taskforge.integration;


import app.bola.taskforge.domain.context.TenantContext;
import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.TaskForgeException;
import app.bola.taskforge.repository.InvitationRepository;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.service.MemberService;
import app.bola.taskforge.service.OrganizationService;
import app.bola.taskforge.service.dto.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Organization functionality.
 * 
 * <p>
 * What is the business rule or expected behavior?
 * "Organizations should be created, updated, and members should be invited properly."
 * </p>
 * <p>
 * What could go wrong (edge cases, errors)?
 * "Duplicate organizations, invalid data, missing required fields, non-existent organizations or users."
 * </p>
 * <p>
 * What are the dependencies?
 * "OrganizationService, OrganizationRepository, UserRepository, InvitationRepository"
 * </p>
 * <p>
 * Is the logic pure or stateful?
 * "Stateful - requires database interaction"
 * </p>
 */

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrganizationIntegrationTest {
    
    private static final Logger log = LoggerFactory.getLogger(OrganizationIntegrationTest.class);
    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    private Member testMember;
    private String uniqueIdentifier;
	@Autowired
	private MemberService memberService;
    
    @BeforeEach
    void setUp() {
        // Generate a unique identifier for test data to avoid conflicts
        uniqueIdentifier = UUID.randomUUID().toString().substring(0, 8);

        // Create a test user for invitation tests
        testMember = Member.builder()
                .email("test.user." + uniqueIdentifier + "@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password123")
                .active(true)
                .build();

        testMember = userRepository.save(testMember);
    }

    @Nested
    @DisplayName("Organization Creation Tests")
    class OrganizationCreationTests {

        @Test
        @DisplayName("Should create organization with valid data")
        void shouldCreateOrganizationWithValidData() {
            // Given
            OrganizationRequest request = createValidOrganizationRequest();

            // When
            OrganizationResponse response = organizationService.createNew(request);
            
            log.info("Created organization: {}", response);
            // Then
            assertNotNull(response);
            assertNotNull(response.getPublicId());
            assertEquals(request.getName(), response.getName());
            assertEquals(request.getSlug(), response.getSlug());
            assertEquals(request.getIndustry(), response.getIndustry());
            assertEquals(request.getCountry(), response.getCountry());
            assertEquals(request.getTimeZone(), response.getTimeZone());
            assertEquals(request.getEmail(), response.getEmail());

            // Verify organization was saved to the database
            Optional<Organization> savedOrg = organizationRepository.findByIdScoped(response.getPublicId());
            assertTrue(savedOrg.isPresent());
            assertEquals(request.getName(), savedOrg.get().getName());
        }

        @Test
        @DisplayName("Should throw exception when creating organization with duplicate name")
        void shouldThrowExceptionWhenCreatingOrganizationWithDuplicateName() {
            // Given
            OrganizationRequest request = createValidOrganizationRequest();

            // Create the organization first
            organizationService.createNew(request);

            // When & Then - Try to create another organization with the same name
            TaskForgeException exception = assertThrows(TaskForgeException.class, () -> 
                organizationService.createNew(request)
            );

            assertEquals("Organization with this name already exists", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when creating organization with invalid data")
        void shouldThrowExceptionWhenCreatingOrganizationWithInvalidData() {
            // Given
            OrganizationRequest request = OrganizationRequest.builder()
                    .name("") // Invalid: empty name
                    .slug("invalid slug with spaces") // Invalid: contains spaces
                    .industry("Technology")
                    .country("Nigeria")
                    .timeZone("Africa/Lagos")
                    .email("invalid-email") // Invalid: not a proper email
                    .phone("123") // Invalid: too short
                    .build();

            // When & Then
            Exception exception = assertThrows(Exception.class, () -> 
                organizationService.createNew(request)
            );

            assertNotNull(exception);
        }
    }

    @Nested
    @DisplayName("Member Invitation Tests")
    class MemberInvitationTests {

        private Organization testOrganization;
        OrganizationResponse organizationResponse;

        @BeforeEach
        void setUp() {
            // Create a test organization for invitation tests
            OrganizationRequest request = createValidOrganizationRequest();
            organizationResponse = organizationService.createNew(request);
            
            TenantContext.setCurrentTenant(organizationResponse.getPublicId());
            testOrganization = organizationRepository.findByIdScoped(organizationResponse.getPublicId())
                    .orElseThrow(() -> new RuntimeException("Failed to create test organization"));
        }

        @Test
        @DisplayName("Should invite member successfully")
        void shouldInviteMemberSuccessfully() {
            MemberResponse memberResponse = memberService.createNew(buildMemberRequest());

            // Given
            InvitationRequest request = InvitationRequest.builder()
                    .organizationId(testOrganization.getPublicId())
                    .email("new.member." + uniqueIdentifier + "@example.com")
                    .name("New Member")
                    .invitedBy(memberResponse.getPublicId())
                    .role("ORGANIZATION_MEMBER")
                    .build();

            // When
            InvitationResponse response = organizationService.inviteMember(request);

            // Then
            assertNotNull(response);
            assertNotNull(response.getInvitationLink());
            assertTrue(response.getInvitationLink().contains("token="));
            assertEquals(testOrganization.getPublicId(), response.getOrganizationId());
        }
        
        private MemberRequest buildMemberRequest() {
            return MemberRequest.builder()
                           .email("john.doe." + uniqueIdentifier + "@example.com")
                           .firstName("John")
                           .lastName("Doe")
                           .password("password123")
                           .organizationId(organizationResponse.getPublicId())
                           .build();
        }
        
        @Test
        @DisplayName("Should throw exception when inviting member to non-existent organization")
        void shouldThrowExceptionWhenInvitingMemberToNonExistentOrganization() {
            // Given
            InvitationRequest request = InvitationRequest.builder()
                    .organizationId("non-existent-org-id")
                    .email("new.member." + uniqueIdentifier + "@example.com")
                    .name("New Member")
                    .invitedBy(testMember.getPublicId())
                    .role("ORGANIZATION_MEMBER")
                    .build();

            // When & Then
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> 
                organizationService.inviteMember(request)
            );

            assertTrue(exception.getMessage().contains("Organization not found"));
        }

        @Test
        @DisplayName("Should throw exception when inviting member with invalid data")
        void shouldThrowExceptionWhenInvitingMemberWithInvalidData() {
            // Given
            InvitationRequest request = InvitationRequest.builder()
                    .organizationId(testOrganization.getPublicId())
                    .email("invalid-email") // Invalid email
                    .name("New Member")
                    .invitedBy(testMember.getPublicId())
                    .role("INVALID_ROLE") // Invalid role
                    .build();

            // When & Then
            Exception exception = assertThrows(Exception.class, () -> 
                organizationService.inviteMember(request)
            );

            assertNotNull(exception);
        }

        @Test
        @DisplayName("Should throw exception when inviting already invited member")
        void shouldThrowExceptionWhenInvitingAlreadyInvitedMember() {
            // Given
            String email = "already.invited." + uniqueIdentifier + "@example.com";
            MemberResponse memberResponse = memberService.createNew(buildMemberRequest());

            InvitationRequest request = InvitationRequest.builder()
                    .organizationId(testOrganization.getPublicId())
                    .email(email)
                    .name("Already Invited")
                    .invitedBy(memberResponse.getPublicId())
                    .role("ORGANIZATION_MEMBER")
                    .build();

            // First invitation
            organizationService.inviteMember(request);

            // When & Then - Second invitation to the same email
            TaskForgeException exception = assertThrows(TaskForgeException.class, () -> 
                organizationService.inviteMember(request)
            );
            
            log.info("Exception message: {}", exception.getMessage());
            assertTrue(exception.getMessage().contains("pending invitation"));
        }
	    
		@Test
	    @DisplayName("Should throw TaskForgeException if member is already part of another organization")
	    public void shouldFailIfMemberIsAlreadyPartOfOrganization(){
		    // Given
		    OrganizationRequest secondOrgRequest = OrganizationRequest.builder()
				                                           .email("secondorg@example.com")
				                                           .name("Second Org")
				                                           .slug("second-org")
				                                           .description("A second test organization")
				                                           .industry("OTHER")
				                                           .timeZone("Africa/Lagos")
				                                           .phone("+0123456789")
				                                           .country("Nigeria")
				                                           .logoUrl("https://secondorg.org/logo.png")
				                                           .websiteUrl("https://secondorg.org")
				                                           .build();
		    
		    OrganizationResponse secondOrgResponse = organizationService.createNew(secondOrgRequest);
		    
		    TenantContext.setCurrentTenant(secondOrgResponse.getPublicId());
		    
		    MemberRequest secondOrgMemberRequest = MemberRequest.builder()
				                                           .organizationId(secondOrgResponse.getPublicId())
				                                           .firstName("Second")
				                                           .lastName("Member")
				                                           .email("secondmember@example.com")
				                                           .password("password123")
				                                           .build();
		    
		    MemberResponse secondOrgMember = memberService.createNew(secondOrgMemberRequest);
		    
		    InvitationRequest invRequest = InvitationRequest.builder()
				                                   .invitedBy(secondOrgMember.getPublicId())
				                                   .email(testMember.getEmail())
				                                   .role("ORGANIZATION_MEMBER")
				                                   .name("Test Member")
				                                   .organizationId(secondOrgResponse.getPublicId())
				                                   .build();
		    
		    // When & Then
		    assertThrows(TaskForgeException.class, () -> organizationService.inviteMember(invRequest));
	    }
	
    }

    /**
     * Helper method to create a valid organization request with unique name and slug
     */
    private OrganizationRequest createValidOrganizationRequest() {
        return OrganizationRequest.builder()
                .name("Test Organization " + uniqueIdentifier)
                .slug("test-org-" + uniqueIdentifier)
                .industry("Technology")
                .country("Nigeria")
                .timeZone("Africa/Lagos")
                .email("contact@testorg" + uniqueIdentifier + ".com")
                .phone("+2348012345678")
                .description("A test organization")
                .websiteUrl("https://testorg" + uniqueIdentifier + ".com")
                .logoUrl("https://testorg" + uniqueIdentifier + ".com/logo.png")
                .build();
    }
}
