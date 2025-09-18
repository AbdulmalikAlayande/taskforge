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
import app.bola.taskforge.notification.MailSender;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.security.provider.JwtTokenProvider;
import app.bola.taskforge.service.dto.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;

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
		Member admin = getCurrentAdmin();
		organization.getMembers().add(admin);
		Organization savedEntity = organizationRepository.save(organization);
		
		if (admin.getOrganization() == null) {
			log.warn("Linking organization to admin");
			admin.setOrganization(savedEntity);
			userRepository.save(admin);
		}
		mailSender.sendWelcomeEmail(admin.getFirstName(), admin.getEmail(), organization.getName());
		return toResponse(savedEntity);
	}
	
	private Member getCurrentAdmin() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepository.findByEmail(username)
				       .orElseThrow(() -> new RuntimeException("Admin not found"));
	}
	
	@Override
	public OrganizationResponse update(String publicId, @NonNull OrganizationRequest organizationRequest) {
		return null;
	}
	
	@Override
	public OrganizationResponse findById(String publicId) {
		log.info("Attempting find by ID: {}", publicId);
		Organization organization = organizationRepository.findByIdScoped(publicId)
			.orElseThrow(() -> new EntityNotFoundException("Organization not found"));
		return toResponse(organization);
	}
	
	@Override
	public Collection<OrganizationResponse> findAll() {
		return List.of();
	}
	
	@Override
	public void delete(String publicId) {
	
	}
	
	@Override
	public OrganizationResponse toResponse(Organization organization) {
			if (organization.getProjects() != null && !organization.getProjects().isEmpty()) {
				Set<ProjectResponse> projectResponses = organization.getProjects().stream().map(project -> {
					ProjectResponse projectResponse = modelMapper.map(project, ProjectResponse.class);
					if (project.getDateRange() != null) {
						projectResponse.setStartDate(project.getDateRange().getStartDate());
						projectResponse.setEndDate(project.getDateRange().getEndDate());
					}
					return projectResponse;
				}).collect(Collectors.toSet());
				OrganizationResponse response = modelMapper.map(organization, OrganizationResponse.class);
				response.setProjects(projectResponses);
				return response;
			}
			return modelMapper.map(organization, OrganizationResponse.class);
		}
	
	@Override
	@Transactional
	public InvitationResponse inviteMember(InvitationRequest request) {
		performValidation(validator, request);
		
		log.info("Inviting member with email: {} to organization with ID: {}", request.getEmail(), request.getOrganizationId());
		
		userRepository.findByEmail(request.getEmail()).ifPresent(entity -> {
			throw new TaskForgeException("User with email %s already exists and belongs to %s org"
				.formatted(request.getEmail(), entity.getOrganization().getName()));
		});
		
		invitationRepository.findByEmail(request.getEmail()).ifPresent(invitation -> {
			if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
				throw new TaskForgeException("User with email %s already accepted an invitation"
					.formatted(request.getEmail()));
			} else if (invitation.getStatus() == InvitationStatus.PENDING || 
					  invitation.getExpiresAt().isAfter(LocalDateTime.now())) {
				throw new TaskForgeException("User with email %s already has a pending invitation"
					.formatted(request.getEmail()));
			}
		});
		
		Organization organization = organizationRepository.findByIdScoped(request.getOrganizationId())
			.orElseThrow(() -> new EntityNotFoundException(
				"Organization not found:: Identifier: " + request.getOrganizationId()));
		
		Member invitedBy = userRepository.findByIdScoped(request.getInvitedBy()).orElse(null);
		if (invitedBy == null && !Objects.equals(request.getRole(), Role.ORGANIZATION_ADMIN.name())) {
			throw new EntityNotFoundException("Member(InvitedBy) with id: %s not found"
				.formatted(request.getInvitedBy()));
		}
		Map<String, Object> claims = Map.of(
			"subject", "Invitation Acceptance", 
			"email", request.getEmail(), 
			"name", request.getInviteeName(),  
			"id", organization.getPublicId(),
			"date", LocalDateTime.now().toString(),
			"role", request.getRole(),
			"organizationName", organization.getName(),
			"organizationLogoUrl", organization.getLogoUrl()
		);
		String token = jwtTokenProvider.generateToken(claims);
		
		
		String base64Token = Base64.getEncoder().encodeToString(token.getBytes());
		
		Invitation invitation = modelMapper.map(request, Invitation.class);
		invitation.setRoles(Set.of(Role.valueOf(request.getRole().toUpperCase())));
		invitation.setOrganization(organization);
		invitation.setInvitedBy(invitedBy);
		invitation.setToken(token);  
		invitation.setStatus(InvitationStatus.PENDING);
		invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
		invitation.setInvitationLink(String.format("https://taskforge.com/%s/accept/%s", 
			organization.getName().toLowerCase().replaceAll("\\s+", "-"), 
			base64Token));
		
		invitationRepository.save(invitation);
		mailSender.sendInvitationMail(invitation, organization.getName());

		InvitationResponse response = modelMapper.map(invitation, InvitationResponse.class);
		response.setMessage("invited");
		response.setOrganizationId(organization.getPublicId());
		response.setOrganizationName(organization.getName());
		log.info("Invitation Token: {}", token);
		return response;
	}
	
	@Override
	public Set<MemberResponse> getMembers(String publicId) {
		Set<Member> members = organizationRepository.findByIdScoped(publicId)
													.orElseThrow(() -> new EntityNotFoundException("Organization not found with public ID: " + publicId))
													.getMembers();
		
		if (members == null || members.isEmpty()) {
			members = new HashSet<>(userRepository.findAllByOrganization_PublicId(publicId));
		}
		
		return members.stream()
				.map(member -> modelMapper.map(member, MemberResponse.class))
				.collect(Collectors.toSet());
	}
	
}
