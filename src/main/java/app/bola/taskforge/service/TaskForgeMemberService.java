package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Invitation;
import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.enums.InvitationStatus;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.exception.TaskForgeException;
import app.bola.taskforge.repository.InvitationRepository;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.security.provider.JwtTokenProvider;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.MemberRequest;
import app.bola.taskforge.service.dto.MemberResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import jakarta.validation.Validator;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class TaskForgeMemberService implements MemberService {

	private final JwtTokenProvider jwtTokenProvider;
	private final InvitationRepository invitationRepository;
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	private final Validator validator;
	private final OrganizationRepository organizationRepository;

	@Override
	@Transactional
	public MemberResponse createNew(@NonNull MemberRequest memberRequest) {

		performValidation(validator, memberRequest);

		Optional<Invitation> optionalInvitation = invitationRepository.findByEmail(memberRequest.getEmail());
		Invitation invitation = checkInvitationPresence(memberRequest, optionalInvitation);
		
		Organization organization = organizationRepository.findByIdScoped(memberRequest.getOrganizationId())
				.orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + memberRequest.getOrganizationId()));

		Member member = modelMapper.map(memberRequest, Member.class);
		member.setActive(true);
		member.setRole(invitation.getRole());
		member.setOrganization(organization);
		
		Member savedMember = userRepository.save(member);
		return toResponse(savedMember);
	}
	
	private Invitation checkInvitationPresence(MemberRequest memberRequest, Optional<Invitation> optionalInvitation) {
		Invitation invitation;
		if (optionalInvitation.isPresent()) {
			invitation = optionalInvitation.get();
			if (invitation.getStatus() == InvitationStatus.PENDING) {
				throw new InvalidRequestException("You have a pending Invitation, please accept the invitation");
			} else if (invitation.getStatus() == InvitationStatus.EXPIRED || invitation.getStatus() == InvitationStatus.REVOKED) {
				throw new InvalidRequestException("Invitation is expired or revoked, please request a new invitation");
			}
		}else {
			throw new InvalidRequestException("No invitation found for email: " + memberRequest.getEmail());
		}
		return invitation;
	}
	
	@Override
	public MemberResponse update(String publicId, @NonNull MemberRequest memberRequest) {
		return null;
	}
	
	@Override
	public MemberResponse findById(String publicId) {
		return null;
	}
	
	@Override
	public Set<MemberResponse> findAll() {
		return Set.of();
	}
	
	@Override
	public void deleteById(String publicId) {
	
	}
	
	@Override
	public MemberResponse toResponse(Member entity) {
		return modelMapper.map(entity, MemberResponse.class);
	}

	@Override
	@Transactional
	public InvitationResponse acceptInvitation(String token) {
		log.info("Accepting invitation with token: {}", token);
		if (jwtTokenProvider.isValidToken(token)) {
			if (jwtTokenProvider.isExpiredToken(token)) {
				throw new InvalidRequestException("Invitation token is expired");
			}
		} else {
			throw new InvalidRequestException("Invalid invitation token");
		}

		String email = jwtTokenProvider.extractClaimFromToken(token);

		Invitation invitation = invitationRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("Invitation does not exist for email: " + email));

		if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new InvalidRequestException("Invitation has expired");
		}

		if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
			throw new TaskForgeException("Invitation has already been accepted, please login");
		}

		if (invitation.getOrganization() == null) {
			throw new InvalidRequestException("Organization does not exist for this invitation");
		}

		Optional<Member> optionalMember = userRepository.findByEmail(email);
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
		response.setEmail(invitation.getEmail());
		return response;
	}
}
