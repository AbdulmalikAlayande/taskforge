package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Invitation;
import app.bola.taskforge.domain.entity.User;
import app.bola.taskforge.domain.enums.InvitationStatus;
import app.bola.taskforge.domain.enums.Role;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.exception.TaskForgeException;
import app.bola.taskforge.repository.InvitationRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.security.provider.JwtTokenProvider;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.MemberRequest;
import app.bola.taskforge.service.dto.UserResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import jakarta.validation.Validator;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskForgeMemberService implements MemberService {

	private final JwtTokenProvider jwtTokenProvider;
	private final InvitationRepository invitationRepository;
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	private final Validator validator;
	
	@Override
	public UserResponse createNew(@NonNull MemberRequest memberRequest) {
		
		performValidation(validator, memberRequest, MemberRequest.class);
	
		User member = modelMapper.map(memberRequest, User.class);
		member.setActive(true);
		member.setRole(Role.ORGANIZATION_MEMBER);

		User savedMember = userRepository.save(member);

		return toResponse(savedMember);
	}
	
	@Override
	public UserResponse toResponse(User entity) {
		return modelMapper.map(entity, UserResponse.class);
	}
	
	@Override
	public InvitationResponse acceptInvitation(String token) {

		if (jwtTokenProvider.isValidToken(token)) {
			if (jwtTokenProvider.isExpiredToken(token)) {
				throw new InvalidRequestException("Invitation token is expired");
			}
		} else {
			throw new InvalidRequestException("Invalid invitation token");
		}

		String email = jwtTokenProvider.extractEmailFromToken(token);

		Invitation invitation = invitationRepository.findByInviteeEmail(email)
                    .orElseThrow(() -> new InvalidRequestException("Invitation does not exist for email: " + email));
		
		if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new InvalidRequestException("Invitation has expired");
		}

		if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
			throw new TaskForgeException("Invitation has already been accepted, please login");
		}

		if (invitation.getOrganization() == null) {
			throw new InvalidRequestException("Organization does not exist for this invitation");
		}
		
		Optional<User> optionalMember = userRepository.findByEmail(email);
		if (optionalMember.isPresent()) {
			if (optionalMember.get().getOrganization().equals(invitation.getOrganization())) {
				throw new InvalidRequestException("Member already part of organization");
			}
			else optionalMember.get().setOrganization(invitation.getOrganization());
			userRepository.save(optionalMember.get());
		}
		invitation.setStatus(InvitationStatus.ACCEPTED);
		
		InvitationResponse response = modelMapper.map(invitation, InvitationResponse.class);
		response.setMessage("Invitation accepted successfully, please create your account");
		response.setOrganizationId(invitation.getOrganization().getPublicId());
		response.setMemberEmail(invitation.getInviteeEmail());
		return response;
	}
}
