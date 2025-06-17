package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Invitation;
import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.enums.InvitationStatus;
import app.bola.taskforge.domain.enums.Role;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.exception.TaskForgeException;
import app.bola.taskforge.repository.InvitationRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.security.provider.JwtTokenProvider;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.MemberRequest;
import app.bola.taskforge.service.dto.MemberResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(value = MockitoExtension.class)
public class MemberServiceTest {

	@Mock
	private ModelMapper modelMapper;
	@Mock
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private InvitationRepository invitationRepository;
	@Mock
	private UserRepository userRepository;
	@InjectMocks
	private TaskForgeMemberService memberService;

	@Nested
	@DisplayName("Accept invitation tests, should test all edge cases related to accepting an invitation after being to an organization")
	class AcceptInvitationTests{

		@Test
		@DisplayName("should accept a valid invitation")
		public void shouldAcceptValidInvitation() {

			Organization organization = mock(Organization.class);
			when(organization.getPublicId()).thenReturn("org-id-123");

			Invitation invitation = mock(Invitation.class);
			when(invitation.getOrganization()).thenReturn(organization);
			when(invitation.getEmail()).thenReturn("email@gmail.com");

			//Given:
			InvitationResponse response = InvitationResponse.builder()
					                              .email("email@gmail.com")
					                              .organizationId("org-id-123")
					                              .message("Invitation accepted successfully").build();

			when(jwtTokenProvider.isValidToken("some-token")).thenReturn(Boolean.TRUE);
			when(jwtTokenProvider.isExpiredToken("some-token")).thenReturn(Boolean.FALSE);
			when(jwtTokenProvider.extractClaimFromToken("some-token")).thenReturn("email@gmail.com");
			when(invitationRepository.findByEmail("email@gmail.com")).thenReturn(Optional.of(invitation));
			when(modelMapper.map(any(Invitation.class), eq(InvitationResponse.class))).thenReturn(response);
			when(invitation.getOrganization().getPublicId()).thenReturn("org-id-123");
			when(invitation.getEmail()).thenReturn("email@gmail.com");

			InvitationResponse invitationResponse = memberService.acceptInvitation("some-token");
			assertNotNull(invitationResponse);
			assertEquals("Invitation accepted successfully", invitationResponse.getMessage());

		}

		@Test
		@DisplayName("should throw InvalidRequestException if invitation is expired")
		public void shouldThrowInvalidRequestExceptionIfInvitationExpired() {
		    // Given
		    when(jwtTokenProvider.isValidToken("expired-token")).thenReturn(Boolean.TRUE);
		    when(jwtTokenProvider.isExpiredToken("expired-token")).thenReturn(Boolean.TRUE);
		
		    // When & Then
		    assertThrows(InvalidRequestException.class, () -> memberService.acceptInvitation("expired-token"));
		}
		
		@Test
		@DisplayName("should throw InvalidRequestException if invitation does not exist")
		public void shouldThrowInvalidRequestExceptionIfInvitationDoesNotExist() {
		    // Given
		    when(jwtTokenProvider.isValidToken("valid-token")).thenReturn(Boolean.TRUE);
		    when(jwtTokenProvider.isExpiredToken("valid-token")).thenReturn(Boolean.FALSE);
		    when(jwtTokenProvider.extractClaimFromToken("valid-token")).thenReturn("nonexistent@gmail.com");
		    when(invitationRepository.findByEmail("nonexistent@gmail.com")).thenReturn(Optional.empty());
		
		    // When & Then
		    assertThrows(InvalidRequestException.class, () -> memberService.acceptInvitation("valid-token"));
		}
		
		@Test
		@DisplayName("should throw TaskForgeException if invitation is already accepted")
		public void shouldThrowTaskForgeExceptionIfInvitationAlreadyAccepted() {
		    // Given
		    Organization organization = mock(Organization.class);
		
		    Invitation invitation = mock(Invitation.class);
		    when(invitation.getStatus()).thenReturn(InvitationStatus.ACCEPTED);
		
		    when(jwtTokenProvider.isValidToken("valid-token")).thenReturn(Boolean.TRUE);
		    when(jwtTokenProvider.isExpiredToken("valid-token")).thenReturn(Boolean.FALSE);
		    when(jwtTokenProvider.extractClaimFromToken("valid-token")).thenReturn("accepted@gmail.com");
		    when(invitationRepository.findByEmail("accepted@gmail.com")).thenReturn(Optional.of(invitation));
		
		    // When & Then
		    assertThrows(TaskForgeException.class, () -> memberService.acceptInvitation("valid-token"));
		}
		
		@Test
		@DisplayName("should throw InvalidRequestException if organization does not exist")
		public void shouldThrowInvalidRequestExceptionIfOrganizationDoesNotExist() {
		    // Given
		    Invitation invitation = mock(Invitation.class);
		    when(invitation.getOrganization()).thenReturn(null);
		    when(invitation.getStatus()).thenReturn(InvitationStatus.PENDING);
		
		    when(jwtTokenProvider.isValidToken("valid-token")).thenReturn(Boolean.TRUE);
		    when(jwtTokenProvider.isExpiredToken("valid-token")).thenReturn(Boolean.FALSE);
		    when(jwtTokenProvider.extractClaimFromToken("valid-token")).thenReturn("email@gmail.com");
		    when(invitationRepository.findByEmail("email@gmail.com")).thenReturn(Optional.of(invitation));
		
		    // When & Then
		    assertThrows(InvalidRequestException.class, () -> memberService.acceptInvitation("valid-token"));
		}
		
		@Test
		@DisplayName("should throw InvalidRequestException if email used in invitation does not match email of accepting user")
		public void shouldThrowInvalidRequestExceptionIfEmailDoesNotMatch() {
		    // Given
		    Invitation invitation = mock(Invitation.class);
		    when(invitation.getEmail()).thenReturn("different@gmail.com");
		    when(invitation.getStatus()).thenReturn(InvitationStatus.PENDING);
		
		    when(jwtTokenProvider.isValidToken("valid-token")).thenReturn(Boolean.TRUE);
		    when(jwtTokenProvider.isExpiredToken("valid-token")).thenReturn(Boolean.FALSE);
		    when(jwtTokenProvider.extractClaimFromToken("valid-token")).thenReturn("email@gmail.com");
		    when(invitationRepository.findByEmail("email@gmail.com")).thenReturn(Optional.of(invitation));
		
		    // When & Then
		    assertThrows(InvalidRequestException.class, () -> memberService.acceptInvitation("valid-token"));
		}
	}
	
	@Nested
	@DisplayName("Create member tests, should test all edge cases related to creating a member of an organization")
	class CreateMemberTests {
		
		@Test
		@DisplayName("should create member with valid data")
		void shouldCreateMemberWithValidData() {
		    // Given
		    MemberRequest request = MemberRequest.builder()
		            .email("test@example.com")
		            .password("password123")
		            .firstName("John")
		            .lastName("Doe")
		            .build();
		
		    Member savedMember = Member.builder()
		            .email("test@example.com")
		            .password("password123")
		            .firstName("John")
		            .lastName("Doe")
		            .active(true)
		            .role(Role.ORGANIZATION_MEMBER)
		            .build();
		
		    MemberResponse expectedResponse = MemberResponse.builder()
		            .email("test@example.com")
		            .firstName("John")
		            .lastName("Doe")
		            .active(true)
		            .role(Role.ORGANIZATION_MEMBER)
		            .build();
		
		    when(userRepository.save(any(Member.class))).thenReturn(savedMember);
		    when(modelMapper.map(savedMember, MemberResponse.class)).thenReturn(expectedResponse);
		
		    // When
		    MemberResponse response = memberService.createNew(request);
		
		    // Then
		    assertNotNull(response);
		    assertEquals("test@example.com", response.getEmail());
		    assertEquals("John", response.getFirstName());
		    assertEquals("Doe", response.getLastName());
		    assertTrue(response.isActive());
		    assertEquals(Role.ORGANIZATION_MEMBER, response.getRole());
		
		    verify(userRepository).save(any(Member.class));
		    verify(modelMapper).map(savedMember, MemberResponse.class);
		}
		
		@Test
		@DisplayName("should throw InvalidRequestException if required fields are missing")
		void shouldThrowInvalidRequestExceptionIfRequiredFieldsMissing() {
		    // Test with null email
		    MemberRequest requestWithNullEmail = MemberRequest.builder()
		            .password("password123")
		            .firstName("John")
		            .lastName("Doe")
		            .build();
		
		    assertThrows(InvalidRequestException.class, () -> memberService.createNew(requestWithNullEmail));
		
		    // Test with empty email
		    MemberRequest requestWithEmptyEmail = MemberRequest.builder()
		            .email("")
		            .password("password123")
		            .firstName("John")
		            .lastName("Doe")
		            .build();
		
		    assertThrows(InvalidRequestException.class, () -> memberService.createNew(requestWithEmptyEmail));
		
		    // Test with null password
		    MemberRequest requestWithNullPassword = MemberRequest.builder()
		            .email("test@example.com")
		            .firstName("John")
		            .lastName("Doe")
		            .build();
		
		    assertThrows(InvalidRequestException.class, () -> memberService.createNew(requestWithNullPassword));
		
		    // Test with empty password
		    MemberRequest requestWithEmptyPassword = MemberRequest.builder()
		            .email("test@example.com")
		            .password("")
		            .firstName("John")
		            .lastName("Doe")
		            .build();
		
		    assertThrows(InvalidRequestException.class, () -> memberService.createNew(requestWithEmptyPassword));
		
		    // Verify that repository was not called
		    verify(userRepository, never()).save(any(Member.class));
		}
		
		@Test
		@DisplayName("should throw NotFoundException if organization does not exist")
		void shouldThrowNotFoundExceptionIfOrganizationDoesNotExist() {
	
		}
	
		@Test
		@DisplayName("should throw ConflictException if member already exists in organization")
		void shouldThrowConflictExceptionIfMemberAlreadyExists() {
	
		}
	
		@Test
		@DisplayName("should throw InvalidRequestException if role is invalid")
		void shouldThrowInvalidRequestExceptionIfRoleIsInvalid() {
	
		}
	
		@Test
		@DisplayName("should throw InvalidRequestException if organization is inactive")
		void shouldThrowInvalidRequestExceptionIfOrganizationIsInactive() {
	
		}
		
		@Test
		@DisplayName("should not call repository if validation fails")
		void shouldNotCallRepositoryIfValidationFails() {
		    // Given
		    MemberRequest invalidRequest = MemberRequest.builder()
		            .email("")  // Empty email will fail validation
		            .password("password123")
		            .firstName("John")
		            .lastName("Doe")
		            .build();
		
		    // When & Then
		    assertThrows(InvalidRequestException.class, () -> memberService.createNew(invalidRequest));
		
		    // Verify repository was not called
		    verify(userRepository, never()).save(any(Member.class));
		}
	}
}
