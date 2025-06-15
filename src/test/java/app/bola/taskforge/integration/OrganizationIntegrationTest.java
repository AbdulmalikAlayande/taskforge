package app.bola.taskforge.integration;


import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.entity.User;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.TaskForgeException;
import app.bola.taskforge.repository.InvitationRepository;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.service.OrganizationService;
import app.bola.taskforge.service.dto.InvitationRequest;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.OrganizationRequest;
import app.bola.taskforge.service.dto.OrganizationResponse;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
 * 
 * <h2>Running the Tests</h2>
 * <p>
 * These tests require a running database. Make sure your application.properties or application.yml
 * is properly configured with database connection details. For integration tests, it's recommended
 * to use an in-memory database like H2 or a containerized database like TestContainers.
 * </p>
 * 
 * <p>
 * If you encounter issues with the Spring Boot test configuration, consider:
 * <ul>
 *   <li>Checking your application.properties/application.yml configuration</li>
 *   <li>Ensuring all required dependencies are available</li>
 *   <li>Verifying that your database is accessible</li>
 *   <li>Looking for conflicting bean definitions</li>
 * </ul>
 * </p>
 * 
 * <p>
 * To run these tests, you can use:
 * <pre>
 * ./mvnw test -Dtest=OrganizationIntegrationTest
 * </pre>
 * or run them directly from your IDE.
 * </p>
 */

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrganizationIntegrationTest {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    private User testUser;
    private String uniqueIdentifier;

    @BeforeEach
    void setUp() {
        // Generate a unique identifier for test data to avoid conflicts
        uniqueIdentifier = UUID.randomUUID().toString().substring(0, 8);

        // Create a test user for invitation tests
        testUser = User.builder()
                .email("test.user." + uniqueIdentifier + "@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password123")
                .active(true)
                .build();

        testUser = userRepository.save(testUser);
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

            // Then
            assertNotNull(response);
            assertNotNull(response.getPublicId());
            assertEquals(request.getName(), response.getName());
            assertEquals(request.getSlug(), response.getSlug());
            assertEquals(request.getIndustry(), response.getIndustry());
            assertEquals(request.getCountry(), response.getCountry());
            assertEquals(request.getTimeZone(), response.getTimeZone());
            assertEquals(request.getContactEmail(), response.getContactEmail());

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
                    .contactEmail("invalid-email") // Invalid: not a proper email
                    .contactPhone("123") // Invalid: too short
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

        @BeforeEach
        void setUp() {
            // Create a test organization for invitation tests
            OrganizationRequest request = createValidOrganizationRequest();
            OrganizationResponse response = organizationService.createNew(request);

            testOrganization = organizationRepository.findByIdScoped(response.getPublicId())
                    .orElseThrow(() -> new RuntimeException("Failed to create test organization"));
        }

        @Test
        @DisplayName("Should invite member successfully")
        void shouldInviteMemberSuccessfully() {
            // Given
            InvitationRequest request = InvitationRequest.builder()
                    .organizationId(testOrganization.getPublicId())
                    .inviteeEmail("new.member." + uniqueIdentifier + "@example.com")
                    .inviteeName("New Member")
                    .invitedBy(testUser.getPublicId())
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

        @Test
        @DisplayName("Should throw exception when inviting member to non-existent organization")
        void shouldThrowExceptionWhenInvitingMemberToNonExistentOrganization() {
            // Given
            InvitationRequest request = InvitationRequest.builder()
                    .organizationId("non-existent-org-id")
                    .inviteeEmail("new.member." + uniqueIdentifier + "@example.com")
                    .inviteeName("New Member")
                    .invitedBy(testUser.getPublicId())
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
                    .inviteeEmail("invalid-email") // Invalid email
                    .inviteeName("New Member")
                    .invitedBy(testUser.getPublicId())
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

            InvitationRequest request = InvitationRequest.builder()
                    .organizationId(testOrganization.getPublicId())
                    .inviteeEmail(email)
                    .inviteeName("Already Invited")
                    .invitedBy(testUser.getPublicId())
                    .role("ORGANIZATION_MEMBER")
                    .build();

            // First invitation
            organizationService.inviteMember(request);

            // When & Then - Second invitation to the same email
            TaskForgeException exception = assertThrows(TaskForgeException.class, () -> 
                organizationService.inviteMember(request)
            );

            assertTrue(exception.getMessage().contains("User already invited"));
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
                .contactEmail("contact@testorg" + uniqueIdentifier + ".com")
                .contactPhone("+2348012345678")
                .description("A test organization")
                .websiteUrl("https://testorg" + uniqueIdentifier + ".com")
                .logoUrl("https://testorg" + uniqueIdentifier + ".com/logo.png")
                .build();
    }
}
