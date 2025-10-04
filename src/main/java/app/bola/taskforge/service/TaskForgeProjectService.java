package app.bola.taskforge.service;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.domain.entity.DateRange;
import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.entity.Project;
import app.bola.taskforge.domain.enums.ProjectStatus;
import app.bola.taskforge.event.publisher.EventPublisher;
import app.bola.taskforge.event.ProjectEvent;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.repository.ProjectRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.service.dto.ProjectRequest;
import app.bola.taskforge.service.dto.ProjectResponse;
import app.bola.taskforge.service.dto.MemberResponse;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class TaskForgeProjectService implements ProjectService{
	
	
	private final ModelMapper modelMapper;
	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;
	private final ProjectRepository projectRepository;
	private final Validator validator;
	private final EventPublisher eventPublisher;
	

	@Override
	public ProjectResponse createNew(@NonNull ProjectRequest projectRequest) {
		performValidation(validator, projectRequest);
		Organization organization = organizationRepository.findByIdScoped(projectRequest.getOrganizationId())
				                            .orElseThrow(() -> new EntityNotFoundException("Organization not found with id: " + projectRequest.getOrganizationId()));
		Member teamLead = null;
		if (StringUtils.isNotBlank(projectRequest.getTeamLeadId())) {
			teamLead = userRepository.findByIdScoped(projectRequest.getTeamLeadId()).orElse(null);
		}
		
		List<Member> members = userRepository.findAllByIdScoped(projectRequest.getMemberIds());
		
		if (members.size() != projectRequest.getMemberIds().size()) {
			try {
				if (members.isEmpty()) {
					throw new EntityNotFoundException("No members found for the provided member IDs.");
				}
				if (members.size() < projectRequest.getMemberIds().size()) {
					throw new EntityNotFoundException("Some member IDs do not correspond to existing members.");
				}
			} catch (EntityNotFoundException exception) {
				log.error(exception.getMessage(), exception);
			}
		}
		
		Project project = modelMapper.map(projectRequest, Project.class);
		project.setOrganization(organization);
		project.setStatus(ProjectStatus.ACTIVE);
		project.setTeamLead(teamLead);
		project.setMembers(new HashSet<>(members));
		project.setDateRange(new DateRange(projectRequest.getStartDate(), projectRequest.getEndDate()));
		
		Project savedProject = projectRepository.save(project);
		eventPublisher.publishEvent(createProjectEvent(savedProject, organization));
		return toResponse(savedProject);
	}
	
	private static ProjectEvent createProjectEvent(Project project, Organization organization) {
		ProjectEvent projectEvent = new ProjectEvent(project, project.getPublicId(), "create");
		projectEvent.setInitiatorId("SYSTEM");
		projectEvent.setMetadata(null);
		projectEvent.setOrganizationId(organization.getPublicId());
		projectEvent.setUserIdList(project.getMembers().stream().map(BaseEntity::getPublicId).toList());
		projectEvent.setUserEmailList(project.getMembers().stream().map(Member::getEmail).toList());
		projectEvent.setDateTimeStamp(LocalDateTime.now());
		return projectEvent;
	}
	
	@Override
	public ProjectResponse update(String publicId, @NonNull ProjectRequest request) {
		Project project = projectRepository.findByIdScoped(publicId)
		                  .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + publicId));
		
		if (request.getName() != null && !request.getName().isEmpty()) {
			project.setName(request.getName());
		}
		if (request.getDescription() != null && !request.getDescription().isEmpty()) {
			project.setDescription(request.getDescription());
		}
		if (request.getStartDate() != null && request.getEndDate() != null){
			if (request.getStartDate().isBefore(LocalDate.now())){
				throw new InvalidRequestException("Invalid Project Start Date: Are you planning to go back in time?");
			}
			if (request.getEndDate().isBefore(LocalDate.now())){
				throw new InvalidRequestException("Invalid Project End Date: Are you planning to go back in time?");
			}
			if (request.getStartDate().isAfter(request.getEndDate())){
				throw new InvalidRequestException("Invalid Date Range: You can't start a project on a date later than the end date.");
			}
			project.setDateRange(new DateRange(request.getStartDate(), request.getEndDate()));
		}
		if (request.getCategory() != null) {
			try {
				project.setCategory(request.getCategory());
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Category '%s' is invalid".formatted(request.getCategory()), e);
			}
		}
		
		Project updatedProject = projectRepository.save(project);
		return toResponse(updatedProject);
	}
	
	@Override
	public ProjectResponse addMember(@NonNull String projectId, @NonNull String memberId) {
		Optional<Member> optionalMember = userRepository.findByEmail(memberId);
		Member member = optionalMember.orElseGet(() -> userRepository.findByIdScoped(memberId)
				                                        .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId)));
		Project project = projectRepository.findByIdScoped(projectId)
				.orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));
		
		if (project.getMembers().contains(member)) {
			throw new InvalidRequestException("Member already exists in the project");
		}
		
		log.info("Project Members Size: {}", project.getMembers().size());
		Set<Member> members = project.getMembers();
		
		if (members != null) {
			members.add(member);
		} else {
			members = new HashSet<>();
			members.add(member);
		}
		
		project.setMembers(members);
		
		Project savedProject = projectRepository.save(project);
		return toResponse(savedProject);
	}
	
	@Override
	public ProjectResponse removeMember(@NonNull String projectId, @NonNull String memberId) {
		Member member = userRepository.findByIdScoped(memberId)
				              .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));
		
		Project project = projectRepository.findByIdScoped(projectId)
				                  .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));
		
		project.getMembers().remove(member);
		Project savedProject = projectRepository.save(project);
		
		return toResponse(savedProject);
	}
	
	@Override
	public ProjectResponse changeStatus(@NonNull String projectId, @NonNull String status) {
		Project project = projectRepository.findByIdScoped(projectId)
				                  .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));
		if (!status.isEmpty()) {
			try {
				project.setStatus(ProjectStatus.valueOf(status.toUpperCase()));
			} catch (IllegalArgumentException exception) {
				throw new InvalidRequestException("Project status '%s' is invalid".formatted(status), exception);
			}
		}
		
		return toResponse(projectRepository.save(project));
	}
	
	@Override
	public Set<ProjectResponse> getAllByOrganizationId(String organizationId) {
		Organization organization = organizationRepository.findByIdScoped(organizationId)
				                            .orElseThrow(() -> new EntityNotFoundException(""));
		List<Project> projects = projectRepository.findAllByOrganization(organization);
		return toResponse(projects);
	}
	
	@Override
	public Set<ProjectResponse> findAll() {
		List<Project> allProjects = projectRepository.findAllScoped();
		return toResponse(
			allProjects.stream().filter(project -> !project.isDeleted()).collect(Collectors.toSet())
		);
	}
	
	@Override
	public ProjectResponse findById(String publicId) {
		Project project = projectRepository.findByIdScoped(publicId)
				                  .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + publicId));
		return project.isDeleted() ? null : toResponse(project);
	}
	
	@Override
	public void delete(String publicId) {
		Project project = projectRepository.findByIdScoped(publicId)
				                  .orElseThrow(() -> new EntityNotFoundException("Project not found"));
		project.setDeleted(true);
		projectRepository.save(project);
	}
	
	@Override
	public Set<MemberResponse> getProjectMembers(String projectId) {
		Project project = projectRepository.findByIdScoped(projectId)
				                  .orElseThrow(() -> new EntityNotFoundException("Project not found"));
		return project.getMembers().stream()
				       .map(member -> modelMapper.map(member, MemberResponse.class))
				       .collect(Collectors.toSet());
	}
	
	@Override
	public Set<ProjectResponse> toResponse(Collection<Project> entities){
		return entities.stream().map(this::toResponse).collect(Collectors.toSet());
	}
	
	@Override
	public ProjectResponse toResponse(Project entity) {
		ProjectResponse response = modelMapper.map(entity, ProjectResponse.class);
		response.setStartDate(entity.getDateRange().getStartDate());
		response.setEndDate(entity.getDateRange().getEndDate());
		return response;
	}
}
