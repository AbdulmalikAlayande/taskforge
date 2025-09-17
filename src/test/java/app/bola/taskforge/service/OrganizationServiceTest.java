package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.exception.TaskForgeException;
import app.bola.taskforge.notification.MailSender;
import app.bola.taskforge.repository.InvitationRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.security.provider.JwtTokenProvider;
import app.bola.taskforge.service.dto.InvitationRequest;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.OrganizationRequest;
import app.bola.taskforge.service.dto.OrganizationResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(value = MockitoExtension.class)
class OrganizationServiceTest {
	
	@Mock
	private Validator validator;
	@Mock
	private ModelMapper modelMapper;
	@Mock
	private MailSender mailSender;
	@Mock
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private UserRepository userRepository;
	@Mock
	private InvitationRepository invitationRepository;
	@Mock
	private OrganizationRepository organizationRepository;
	@InjectMocks
	private TaskForgeOrganizationService organizationService;
	
	@BeforeEach
	public void setUp() {

	}
	
	@Nested
	@DisplayName("Organization creation tests")
	class OrganizationCreationTests {
		
		@Test
		@DisplayName("should create organization if name is unique")
		public void shouldCreateOrganizationIfNameIsUnique() {
			//Given
			String uniqueName = "Ajileye and Sons";
			Organization organization = Organization.builder().name(uniqueName).build();
			when(organizationRepository.existsByName(uniqueName)).thenReturn(false);
			when(modelMapper.map(any(OrganizationRequest.class), eq(Organization.class))).thenReturn(organization);
			when(organizationRepository.save(any())).thenReturn(organization);
			when(modelMapper.map(any(Organization.class), eq(OrganizationResponse.class))).thenReturn(
					OrganizationResponse.builder().name(uniqueName).build()
			);
			
			//When
			OrganizationResponse response = organizationService.createNew(OrganizationRequest.builder().name(uniqueName).build());
			
			//Then
			assertNotNull(response);
			assertEquals(uniqueName, response.getName());
			verify(organizationRepository).save(organization);
		}
		
		@Test
		@DisplayName("should throw exception if organization with name already exists")
		public void shouldThrowExceptionIfOrganizationWithNameAlreadyExists() {
			//Given
			String existingName = "Ajileye and Sons";
			when(organizationRepository.existsByName(existingName)).thenReturn(true);
			
			//When & Then
			TaskForgeException exception = assertThrows(TaskForgeException.class, () -> organizationService.createNew(
					OrganizationRequest.builder()
							.name(existingName)
							.slug("ajileye-and-sons")
							.industry("Technology")
							.country("Nigeria")
							.timeZone("Africa/Lagos")
							.build())
			);
			
			assertEquals("Organization with this name already exists", exception.getMessage());
			verify(organizationRepository).existsByName(existingName);
		}
		
		@Test
		public void shouldMapOrganizationToResponse() {
			// Given
			Organization organization = Organization.builder()
					                            .name("eReach Org")
					                            .slug("e-reach-org")
					                            .country("Nigeria")
					                            .industry("HealthCare")
					                            .publicId("org-12345")
					                            .timeZone("Africa/Lagos")
					                            .description("A healthcare organization focused on community health")
					                            .email("alaabdulmalik03@gmail.com")
					                            .phone("+2348034567890")
					                            .logoUrl("https://ereach.org/logo.png")
					                            .websiteUrl("https://ereach.org")
					                            .build();
			
			// When
			when(modelMapper.map(organization, OrganizationResponse.class))
					.thenReturn(OrganizationResponse.builder()
							            .name("eReach Org")
							            .slug("e-reach-org")
							            .country("Nigeria")
							            .industry("Healthcare")
							            .publicId("org-12345")
							            .timeZone("Africa/Lagos")
							            .description("A healthcare organization focused on community health")
							            .email("alaabdulmalik03@gmail.com")
							            .phone("+2348034567890")
							            .logoUrl("https://ereach.org/logo.png")
							            .websiteUrl("https://ereach.org")
							            .build()
					);
			
			OrganizationResponse response = organizationService.toResponse(organization);
			// Then
			assertNotNull(response);
			assertEquals("eReach Org", response.getName());
			assertThat(response).hasNoNullFieldsOrPropertiesExcept(
					"createdAt", "projects",
					"lastModifiedAt", "members"
			);
			verify(modelMapper).map(organization, OrganizationResponse.class);
		}
		
		@Test
		public void shouldThrowInvalidRequestExceptionIfRequiredFieldsAreMissing() {
			// Given
			OrganizationRequest request = OrganizationRequest.builder()
					                              .name("")
					                              .slug("valid-slug")
					                              .industry("Technology")
					                              .country("Nigeria")
					                              .email(null)
					                              .phone("")
					                              .timeZone(null)
					                              .build();
			
			ConstraintViolation<OrganizationRequest> violation = mock(ConstraintViolation.class);
			
			when(violation.getMessage()).thenReturn("Organization name is required");
			when(validator.validate(request, OrganizationRequest.class)).thenReturn(Set.of(violation));
			
			InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
					                                                                                organizationService.createNew(request)
			);
			assertEquals("Organization name is required", exception.getMessage());
			
			verifyNoInteractions(organizationRepository, modelMapper);
		}
		
		@Test
		public void shouldThrowInvalidRequestExceptionIfFieldsBreakValidationRules() {
			// Given: multiple invalid fields
			OrganizationRequest request = OrganizationRequest.builder()
					                              .name("A")
					                              .slug("Invalid Slug!")
					                              .industry("")
					                              .country("")
					                              .email("not-an-email")
					                              .phone("12345")
					                              .timeZone("Invalid/Zone!")
					                              .websiteUrl("invalid-url")
					                              .logoUrl("invalid-url")
					                              .build();
			
			ConstraintViolation<OrganizationRequest> v1 = mock(ConstraintViolation.class);
			ConstraintViolation<OrganizationRequest> v2 = mock(ConstraintViolation.class);
			ConstraintViolation<OrganizationRequest> v3 = mock(ConstraintViolation.class);
			ConstraintViolation<OrganizationRequest> v4 = mock(ConstraintViolation.class);
			ConstraintViolation<OrganizationRequest> v5 = mock(ConstraintViolation.class);
			ConstraintViolation<OrganizationRequest> v6 = mock(ConstraintViolation.class);
			ConstraintViolation<OrganizationRequest> v7 = mock(ConstraintViolation.class);
			ConstraintViolation<OrganizationRequest> v8 = mock(ConstraintViolation.class);
			ConstraintViolation<OrganizationRequest> v9 = mock(ConstraintViolation.class);
			
			when(v1.getMessage()).thenReturn("Name must be between 2 and 100 characters");
			when(v2.getMessage()).thenReturn("Slug must contain only lowercase letters, numbers, and dashes");
			when(v3.getMessage()).thenReturn("Industry is required");
			when(v4.getMessage()).thenReturn("Country is required");
			when(v5.getMessage()).thenReturn("Contact email must be valid");
			when(v6.getMessage()).thenReturn("Contact phone must be a valid international number");
			when(v7.getMessage()).thenReturn("Time zone format is invalid (e.g., Africa/Lagos)");
			when(v8.getMessage()).thenReturn("Website URL must be valid");
			when(v9.getMessage()).thenReturn("Website URL must be valid");
			
			Set<ConstraintViolation<OrganizationRequest>> violations = Set.of(
					v1, v2, v3, v4, v5, v6, v7, v8, v9
			);
			
			when(validator.validate(request, OrganizationRequest.class)).thenReturn(violations);
			
			// When & Then
			InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
					                                                                                organizationService.createNew(request)
			);
			
			String message = exception.getMessage();
			assertTrue(message.contains("Name must be between 2 and 100 characters"));
			assertTrue(message.contains("Slug must contain only lowercase letters, numbers, and dashes"));
			assertTrue(message.contains("Industry is required"));
			assertTrue(message.contains("Country is required"));
			assertTrue(message.contains("Contact email must be valid"));
			assertTrue(message.contains("Contact phone must be a valid international number"));
			assertTrue(message.contains("Time zone format is invalid (e.g., Africa/Lagos)"));
			assertTrue(message.contains("Website URL must be valid"));
			
			verifyNoInteractions(organizationRepository, modelMapper);
		}
		
		@Test
		@DisplayName("should throw exception if repository save fails")
		public void shouldThrowExceptionIfRepositorySaveFails() {
			// Given
			OrganizationRequest request = OrganizationRequest.builder()
					                              .name("Unique Org")
					                              .slug("unique-org")
					                              .industry("Tech")
					                              .country("Nigeria")
					                              .email("test@email.com")
					                              .phone("+2348012345678")
					                              .timeZone("Africa/Lagos")
					                              .build();
			
			when(validator.validate(request, OrganizationRequest.class)).thenReturn(Set.of());
			when(organizationRepository.existsByName("Unique Org")).thenReturn(false);
			when(modelMapper.map(request, Organization.class)).thenReturn(Organization.builder().name("Unique Org").build());
			when(organizationRepository.save(any())).thenThrow(new RuntimeException("DB error"));
			
			// When & Then
			RuntimeException ex = assertThrows(RuntimeException.class, () -> organizationService.createNew(request));
			assertEquals("DB error", ex.getMessage());
			verify(organizationRepository).save(any());
		}
		
		@Test
		@DisplayName("should throw exception if slug is duplicate")
		public void shouldThrowExceptionIfSlugIsDuplicate() {
			// Given
			OrganizationRequest request = OrganizationRequest.builder()
					                              .name("Another Org")
					                              .slug("duplicate-slug")
					                              .industry("Tech")
					                              .country("Nigeria")
					                              .email("test@email.com")
					                              .phone("+2348012345678")
					                              .timeZone("Africa/Lagos")
					                              .build();
			
			when(validator.validate(request, OrganizationRequest.class)).thenReturn(Set.of());
			when(organizationRepository.existsByName("Another Org")).thenReturn(false);
			when(modelMapper.map(request, Organization.class)).thenReturn(Organization.builder().name("Another Org").slug("duplicate-slug").build());
			when(organizationRepository.save(any())).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Unique index or primary key violation"));
			
			// When & Then
			org.springframework.dao.DataIntegrityViolationException ex = assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> organizationService.createNew(request));
			assertTrue(ex.getMessage().contains("Unique index"));
			verify(organizationRepository).save(any());
		}
		
		@Test
		@DisplayName("should throw exception if organization request is null")
		public void shouldThrowExceptionIfRequestIsNull() {
			// When & Then
			NullPointerException ex = assertThrows(NullPointerException.class, () -> organizationService.createNew(null));
			assertNotNull(ex);
			verifyNoInteractions(organizationRepository, modelMapper);
		}
		
		@Test
		@DisplayName("should trim and normalize name and slug before saving")
		public void shouldTrimAndNormalizeNameAndSlug() {
			// Given
			OrganizationRequest request = OrganizationRequest.builder()
					                              .name("  Org Name  ")
					                              .slug("  org-slug  ")
					                              .industry("Tech")
					                              .country("Nigeria")
					                              .email("test@email.com")
					                              .phone("+2348012345678")
					                              .timeZone("Africa/Lagos")
					                              .build();
			
			Organization trimmedOrg = Organization.builder()
					                          .name("Org Name")
					                          .slug("org-slug")
					                          .build();
			
			when(validator.validate(request, OrganizationRequest.class)).thenReturn(Set.of());
			when(organizationRepository.existsByName("  Org Name  ")).thenReturn(false);
			when(modelMapper.map(request, Organization.class)).thenReturn(trimmedOrg);
			when(organizationRepository.save(trimmedOrg)).thenReturn(trimmedOrg);
			when(modelMapper.map(trimmedOrg, OrganizationResponse.class)).thenReturn(
					OrganizationResponse.builder().name("Org Name").slug("org-slug").build()
			);
			
			// When
			OrganizationResponse response = organizationService.createNew(request);
			
			// Then
			assertNotNull(response);
			assertEquals("Org Name", response.getName());
			assertEquals("org-slug", response.getSlug());
			verify(organizationRepository).save(trimmedOrg);
		}
		
		@Test
		@DisplayName("should set audit fields on organization response")
		public void shouldSetAuditFieldsOnOrganizationResponse() {
			// Given
			Organization org = Organization.builder()
					                   .name("Audit Org")
					                   .slug("audit-org")
					                   .build();
			org.setCreatedAt(java.time.LocalDateTime.now());
			org.setLastModifiedAt(java.time.LocalDateTime.now());
			
			OrganizationResponse response = OrganizationResponse.builder()
					                                .name("Audit Org")
					                                .slug("audit-org")
					                                .createdAt(org.getCreatedAt())
					                                .lastModifiedAt(org.getLastModifiedAt())
					                                .build();
			
			when(modelMapper.map(org, OrganizationResponse.class)).thenReturn(response);
			
			// When
			OrganizationResponse result = organizationService.toResponse(org);
			
			// Then
			assertNotNull(result.getCreatedAt());
			assertNotNull(result.getLastModifiedAt());
			assertEquals(org.getCreatedAt(), result.getCreatedAt());
			assertEquals(org.getLastModifiedAt(), result.getLastModifiedAt());
			verify(modelMapper).map(org, OrganizationResponse.class);
		}
	}
	
	@Nested
	@DisplayName("Member Invitation tests")
	class MemberInvitationTests {
		OrganizationResponse organizationResponse;
		
		@BeforeEach
		public void setUp() {
		
		}
		@Test
		@DisplayName("should invite member if not already invited")
		public void shouldInviteMemberIfNotAlreadyInvited() {
			
			OrganizationRequest orgRequest = OrganizationRequest.builder()
					.name("Swot Inc.").slug("swot-inc").industry("Technology").country("Nigeria")
                    .email("swotinc@gmail.com").phone("+2348034567890")
					.timeZone("Africa/Lagos").description("A tech company focused on innovation")
					.websiteUrl("https://swotinc.com").logoUrl("https://swotinc.com/logo.png")
                    .build();
			
			OrganizationResponse orgResponse = OrganizationResponse.builder()
	                                   .name("Swot Inc.").slug("swot-inc").industry("Technology")
	                                   .country("Nigeria").email("swotinc@gmail.com").phone("+2348034567890")
	                                   .timeZone("Africa/Lagos").description("A tech company focused on innovation")
	                                   .websiteUrl("https://swotinc.com").logoUrl("https://swotinc.com/logo.png")
	                                   .publicId("org-12345").build();
			
			Organization organization = Organization.builder()
			                            .name("Swot Inc.").slug("swot-inc").industry("Technology").country("Nigeria")
		                                .email("swotinc@gmail.com").phone("+2348034567890")
			                            .timeZone("Africa/Lagos").description("A tech company focused on innovation")
			                            .websiteUrl("https://swotinc.com").logoUrl("https://swotinc.com/logo.png")
			                            .publicId("org-12345").build();
			
			Member member = Member.builder().publicId("member-id-123").firstName("Abdulmalik").lastName("Alayande")
					              .email("alaabdulmalik03@gmail.com").password("password-123").build();
			
			
			when(organizationService.createNew(orgRequest)).thenReturn(orgResponse);
			when(organizationRepository.findByIdScoped("org-12345")).thenReturn(Optional.of(organization));
			when(invitationRepository.existsByEmailAndOrganization("alaabdulmalik03@gmail.com", organization)).thenReturn(Boolean.FALSE);
			when(userRepository.findByIdScoped("member-id-123")).thenReturn(Optional.of(member));
			when(jwtTokenProvider.generateToken("Alayande Abdulmalik", "org-12345")).thenReturn("some-jwt-token");
			when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
			when(mailSender.sendEmail(anyList(), anyString(), anyString())).thenReturn(ResponseEntity.ok().build());
		
			//Action: Create organization and invite member
			organizationResponse = organizationService.createNew(orgRequest);

			InvitationRequest request = InvitationRequest.builder()
					.organizationId(organizationResponse.getPublicId())
					.email("alaabdulmalik03@gmail.com")
                    .inviteeName("Alayande Abdulmalik")
					.invitedBy("member-id-123")
                    .role("ORGANIZATION_MEMBER")
                    .build();
			
			InvitationResponse invitationResponse = organizationService.inviteMember(request);
			
			// Assert: Check if the invitation was successful
			assertNotNull(invitationResponse);
			assertTrue(invitationResponse.getInvitationLink().contains("https://taskforge.com/accept?token="));
		}
		
		@Test
		@DisplayName("should throw TaskForgeException if member is already invited")
		public void shouldThrowTaskForgeExceptionIfMemberAlreadyInvited() {
			// Given: setup for an already invited member
			OrganizationRequest orgRequest = OrganizationRequest.builder()
					                                 .name("Swot Inc.").slug("swot-inc").industry("Technology").country("Nigeria")
					                                 .email("swotinc@gmail.com").phone("+2348034567890")
					                                 .timeZone("Africa/Lagos").description("A tech company focused on innovation")
					                                 .websiteUrl("https://swotinc.com").logoUrl("https://swotinc.com/logo.png")
					                                 .build();
			
			Organization organization = Organization.builder().name("Swot Inc.").slug("swot-inc").industry("Technology")
					                            .country("Nigeria").email("swotinc@gmail.com").phone("+2348034567890")
					                            .timeZone("Africa/Lagos").description("A tech company focused on innovation")
					                            .websiteUrl("https://swotinc.com").logoUrl("https://swotinc.com/logo.png")
					                            .publicId("org-public-id-12345").build();
			
			OrganizationResponse orgResponse = OrganizationResponse.builder()
					                                   .name("Swot Inc.").slug("swot-inc").industry("Technology")
					                                   .country("Nigeria").email("swotinc@gmail.com").phone("+2348034567890")
					                                   .timeZone("Africa/Lagos").description("A tech company focused on innovation")
					                                   .websiteUrl("https://swotinc.com").logoUrl("https://swotinc.com/logo.png")
					                                   .publicId("org-public-id-12345").build();
			
			Member member = Member.builder().publicId("member-id-123").firstName("Abdulmalik").lastName("Alayande")
					                                .email("alaabdulmalik03@gmail.com").password("password-123").build();
			
			when(organizationService.createNew(orgRequest)).thenReturn(orgResponse);
			when(organizationRepository.findByIdScoped("org-public-id-12345")).thenReturn(Optional.of(organization));
			when(invitationRepository.existsByEmailAndOrganization("alaabdulmalik03@gmail.com", organization)).thenReturn(Boolean.TRUE);
			
			//Action: Create organization and invite member
			organizationResponse = organizationService.createNew(orgRequest);
			
			InvitationRequest request = InvitationRequest.builder()
					                            .organizationId(organizationResponse.getPublicId())
					                            .email("alaabdulmalik03@gmail.com")
					                            .inviteeName("Alayande Abdulmalik")
					                            .invitedBy("member-id-123")
					                            .role("ORGANIZATION_MEMBER")
					                            .build();
			
			// When: call the inviteMember method
			TaskForgeException exception = assertThrows(TaskForgeException.class, () -> organizationService.inviteMember(request));
			
			// Then: assert that TaskForgeException is thrown with the correct message
			assertEquals("User already invited", exception.getMessage());
		}
		
		@Test
		@DisplayName("should throw EntityNotFoundException if organization does not exist")
		public void shouldThrowEntityNotFoundExceptionIfOrganizationDoesNotExist() {
			// Given: setup for a non-existent organization
			InvitationRequest request = InvitationRequest.builder()
					                            .organizationId("non-existent-org-id")
					                            .email("alaabdulmalik03@gmail.com")
					                            .inviteeName("Alayande Abdulmalik")
					                            .invitedBy("member-id-123")
					                            .role("ORGANIZATION_MEMBER")
					                            .build();
			
			when(organizationRepository.findByIdScoped("non-existent-org-id")).thenReturn(Optional.empty());
			
			// When: call the inviteMember method
			EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
					() -> organizationService.inviteMember(request)
			);
			
			assertEquals("Organization not found:: Identifier: non-existent-org-id", exception.getMessage());
			verifyNoInteractions(invitationRepository, userRepository, mailSender);
		}
		
		@Test
		@DisplayName("should throw exception if email is invalid")
		public void shouldThrowExceptionIfEmailIsInvalid() {
			// Given: setup for an invalid email
			InvitationRequest request = InvitationRequest.builder()
					                            .organizationId("org-12345")
					                            .email("invalid-email-format")
					                            .inviteeName("Alayande Abdulmalik")
					                            .invitedBy("member-id-123")
					                            .role("ORGANIZATION_MEMBER")
					                            .build();
			ConstraintViolation<InvitationRequest> invalidEmailViolation = mock(ConstraintViolation.class);
			
			when(validator.validate(request, InvitationRequest.class)).thenReturn(Set.of(invalidEmailViolation));
			when(invalidEmailViolation.getMessage()).thenReturn("Invalid email format");
			
			// When: call the inviteMember method
			InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> organizationService.inviteMember(request));
			
			// Then: assert that InvalidRequestException is thrown with the correct message
			assertEquals("Invalid email format", exception.getMessage());
			verifyNoInteractions(organizationRepository, invitationRepository, userRepository, mailSender);
		}
		
		@Test
		@DisplayName("should throw InvalidRequestException if required fields are missing")
		public void shouldThrowInvalidRequestExceptionIfRequiredFieldsAreMissing() {
			// Given: setup for an invalid request with missing fields
			InvitationRequest request = InvitationRequest.builder()
					                            .organizationId(null) // missing
					                            .email("")     // missing
					                            .inviteeName(null)    // missing
					                            .invitedBy("")        // missing
					                            .role(null)           // missing
					                            .build();
			
			ConstraintViolation<InvitationRequest> violation = mock(ConstraintViolation.class);
			when(violation.getMessage()).thenReturn("Required fields are missing");
			when(validator.validate(request, InvitationRequest.class)).thenReturn(Set.of(violation));
			
			// When: call the inviteMember method
			InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> organizationService.inviteMember(request));
			
			// Then: assert that InvalidRequestException is thrown with the correct message and verify no repository/mail interactions
			assertEquals("Required fields are missing", exception.getMessage());
			verifyNoInteractions(organizationRepository, invitationRepository, userRepository, mailSender);
		}
		
		@Test
		@DisplayName("should handle exceptions from repository save")
		public void shouldHandleExceptionsFromRepositorySave() {
			// Given: setup for a repository save failure
			// This will include mocking the repository to throw an exception when saving the invitation
			
			// When: call the inviteMember method
			
			// Then: assert that the exception is handled correctly
			// This will include verifying that the correct exception is thrown and that no further actions are taken
		}
	}
	
	@Nested
	@DisplayName("Organization fetching mapping tests")
	class FindOrganizationTests {}
}
