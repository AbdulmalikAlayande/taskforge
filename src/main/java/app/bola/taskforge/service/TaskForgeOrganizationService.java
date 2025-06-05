package app.bola.taskforge.service;

import app.bola.taskforge.service.dto.OrganizationRequest;
import app.bola.taskforge.service.dto.OrganizationResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TaskForgeOrganizationService implements OrganizationService{
	
	
	@Override
	public OrganizationResponse createNew(OrganizationRequest organizationRequest) {
		return null;
	}
}
