package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.DateRange;
import app.bola.taskforge.domain.entity.User;
import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.entity.Project;
import app.bola.taskforge.domain.enums.ProjectCategory;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.service.dto.ProjectRequest;
import app.bola.taskforge.service.dto.ProjectResponse;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class TaskForgeProjectService implements ProjectService{
	
	
	private final ModelMapper modelMapper;
	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;
	
	@PostConstruct
	public void init() {
		modelMapper.addMappings(new PropertyMap<ProjectRequest, Project>() {
			@Override
			protected void configure() {
				map().setCategory(ProjectCategory.valueOf(source.getCategory().toUpperCase()));
				map().setDateRange(new DateRange(source.getStartDate(), source.getEndDate()));
			}
		});
	}
	
	@Override
	public ProjectResponse createNew(@NonNull ProjectRequest projectRequest) {
		
		Organization organization = organizationRepository.findByIdScoped(projectRequest.getOrganizationId())
				                            .orElseThrow(() -> new EntityNotFoundException("Organization not found with id: " + projectRequest.getOrganizationId()));
		
		List<User> members = userRepository.findAllByIdScoped(projectRequest.getMemberIds());
		
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
		project.setMembers(Set.copyOf(members));
		return toResponse(project);
	}
	
	@Override
	public ProjectResponse addMember(String projectId, String memberId) {
		return null;
	}
	
	@Override
	public ProjectResponse removeMember(String projectId, String memberId) {
		return null;
	}
	
	@Override
	public Set<ProjectResponse> getAllByOrganizationId(String organizationId) {
		return Set.of();
	}
	
	@Override
	public ProjectResponse toResponse(Project entity) {
		return modelMapper.map(entity, ProjectResponse.class);
	}
}
