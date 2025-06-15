package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Invitation;
import app.bola.taskforge.domain.entity.User;
import app.bola.taskforge.domain.enums.InvitationStatus;
import app.bola.taskforge.domain.enums.Role;
import app.bola.taskforge.repository.InvitationRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.TaskForgeException;
import app.bola.taskforge.messaging.notification.MailSender;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.security.provider.JwtTokenProvider;
import app.bola.taskforge.service.dto.InvitationRequest;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.OrganizationRequest;
import app.bola.taskforge.service.dto.OrganizationResponse;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class TaskForgeOrganizationService implements OrganizationService {
	private final InvitationRepository invitationRepository;
	
	final UserRepository userRepository;
	final ModelMapper modelMapper;
	final OrganizationRepository organizationRepository;
	final Validator validator;
	final MailSender mailSender;
	private final JwtTokenProvider jwtTokenProvider;
	
	
	@Override
	public OrganizationResponse createNew(@NonNull OrganizationRequest organizationRequest) {
		performValidation(validator, organizationRequest, OrganizationRequest.class);
		
		if (organizationRepository.existsByName(organizationRequest.getName())) {
			log.error("Organization with name {} already exists", organizationRequest.getName());
			throw new TaskForgeException("Organization with this name already exists");
		}
		
		Organization organization = modelMapper.map(organizationRequest, Organization.class);
		Organization savedEntity = organizationRepository.save(organization);
		
		return toResponse(savedEntity);
	}
	
	@Override
	public OrganizationResponse update(String publicId, @NonNull OrganizationRequest organizationRequest) {
		return null;
	}
	
	@Override
	public OrganizationResponse toResponse(Organization organization) {
		return modelMapper.map(organization, OrganizationResponse.class);
	}
	
	@Override
	public InvitationResponse inviteMember(InvitationRequest request) {
		performValidation(validator, request, InvitationRequest.class);
		
		userRepository.findByEmail(request.getInviteeEmail()).ifPresent(entity -> {
			throw new TaskForgeException("User with email %s already exists and belongs to %s org".formatted(request.getInviteeEmail(), entity.getOrganization().getName()));
		});
		
		invitationRepository.findByInviteeEmail(request.getInviteeEmail()).ifPresent(invitation -> {
			if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
				throw new TaskForgeException("User with email %s already accepted an invitation".formatted(request.getInviteeEmail()));
			} else if (invitation.getStatus() == InvitationStatus.PENDING || invitation.getExpiresAt().isAfter(LocalDateTime.now())) {
				throw new TaskForgeException("User with email %s already has a pending invitation".formatted(request.getInviteeEmail()));
			}
		});
		
		Organization organization = organizationRepository.findByIdScoped(request.getOrganizationId())
				                            .orElseThrow(() -> new EntityNotFoundException("Organization not found:: Identifier: " + request.getOrganizationId()));
		
		if (invitationRepository.existsByInviteeEmailAndOrganization(request.getInviteeEmail(), organization)) {
			throw new TaskForgeException("User already invited");
		}
		
		User member = userRepository.findByIdScoped(request.getInvitedBy())
				                .orElseThrow(() -> new EntityNotFoundException("Member(InvitedBy) not found"));
		
		String token = jwtTokenProvider.generateToken(request.getInviteeName(), organization.getPublicId());
		
		Invitation invitation = new Invitation();
		invitation.setRole(Role.valueOf(request.getRole().toUpperCase()));
		invitation.setInviteeEmail(request.getInviteeEmail());
		invitation.setOrganization(organization);
		invitation.setInvitedBy(member);
		invitation.setToken(token);
		invitation.setStatus(InvitationStatus.PENDING);
		invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
		
		invitationRepository.save(invitation);
		
		ResponseEntity<?> response = mailSender.sendEmail(
				List.of(new MailSender.Notification.Recipient(request.getInviteeEmail(), request.getInviteeName())),
				"Invitation to join TaskForge",
				"You have to join %s on TaskForge. Click this link to join https://taskforge.com/accept?token=%s".formatted(organization.getName(), token)
		);
		String body = response != null ? String.valueOf(response.getBody()) : "Email sent successfully";
		return InvitationResponse.builder()
				       .message(body)
				       .invitationLink("https://taskforge.com/accept?token=%s".formatted(token))
				       .build();
	}
	
	// TODO: add acceptInvitation(String token)...
}
