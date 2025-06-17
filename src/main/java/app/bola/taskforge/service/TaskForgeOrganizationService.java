package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Invitation;
import app.bola.taskforge.domain.entity.Member;
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
import java.util.Objects;
import java.util.Optional;

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
		performValidation(validator, organizationRequest);
		
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
		performValidation(validator, request);
		
		userRepository.findByEmail(request.getEmail()).ifPresent(entity -> {
			throw new TaskForgeException("User with email %s already exists and belongs to %s org".formatted(request.getEmail(), entity.getOrganization().getName()));
		});
		
		invitationRepository.findByEmail(request.getEmail()).ifPresent(invitation -> {
			if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
				throw new TaskForgeException("User with email %s already accepted an invitation".formatted(request.getEmail()));
			} else if (invitation.getStatus() == InvitationStatus.PENDING || invitation.getExpiresAt().isAfter(LocalDateTime.now())) {
				throw new TaskForgeException("User with email %s already has a pending invitation".formatted(request.getEmail()));
			}
		});
		
		Organization organization = organizationRepository.findByIdScoped(request.getOrganizationId())
				                            .orElseThrow(() -> new EntityNotFoundException("Organization not found:: Identifier: " + request.getOrganizationId()));
		
		Member invitedBy = null;
		Optional<Member> optionalMember = userRepository.findByIdScoped(request.getInvitedBy());
		if (optionalMember.isEmpty()) {
			if (!Objects.equals(request.getRole(), Role.ORGANIZATION_ADMIN.name()))
				throw new EntityNotFoundException("Member(InvitedBy) with id: %s not found".formatted(request.getInvitedBy()));
		} else {
			invitedBy = optionalMember.get();
		}
		
		String token = jwtTokenProvider.generateToken(request.getEmail(), organization.getPublicId());
		
		Invitation invitation = modelMapper.map(request, Invitation.class);
		invitation.setRole(Role.valueOf(request.getRole().toUpperCase()));
		invitation.setOrganization(organization);
		invitation.setInvitedBy(invitedBy);
		invitation.setToken(token);
		invitation.setStatus(InvitationStatus.PENDING);
		invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
		
		invitationRepository.save(invitation);
		
		ResponseEntity<?> mailResponse = mailSender.sendEmail(
				List.of(new MailSender.Notification.Recipient(request.getEmail(), request.getName())),
				"Invitation to join TaskForge",
				"You have to join %s on TaskForge. Click this link to join https://taskforge.com/accept?token=%s".formatted(organization.getName(), token)
		);
		
		String body = mailResponse != null ? String.valueOf(mailResponse.getBody()) : "Email sent successfully";
		InvitationResponse response = modelMapper.map(invitation, InvitationResponse.class);
		response.setMessage(body);
		response.setOrganizationId(organization.getPublicId());
		response.setOrganizationName(organization.getName());
		response.setInvitationLink("https://taskforge.com/accept?token=%s".formatted(token));
		return response;
	}
	
}
