package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.exception.TaskForgeException;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.service.dto.OrganizationRequest;
import app.bola.taskforge.service.dto.OrganizationResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TaskForgeOrganizationService implements OrganizationService {
	
	final ModelMapper modelMapper;
	final OrganizationRepository organizationRepository;
	
	@Override
	public OrganizationResponse createNew(OrganizationRequest organizationRequest) {
		
		if (organizationRepository.existsByName(organizationRequest.getName())) {
			log.error("Organization with name {} already exists", organizationRequest.getName());
			throw new TaskForgeException("Organization with this name already exists");
		}
		
		log.error("modelMapper: {}", modelMapper);
		
		Organization organization = modelMapper.map(organizationRequest, Organization.class);
		Organization savedEntity = organizationRepository.save(organization);
		
		return toResponse(savedEntity);
	}
	
	@Override
	public OrganizationResponse toResponse(Organization organization) {
		return modelMapper.map(organization, OrganizationResponse.class);
	}
}
