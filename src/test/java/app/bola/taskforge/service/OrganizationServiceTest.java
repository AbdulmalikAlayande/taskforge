package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.service.dto.OrganizationRequest;
import app.bola.taskforge.service.dto.OrganizationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(value = MockitoExtension.class)
class OrganizationServiceTest {
	
	@Mock
	private OrganizationRepository organizationRepository;
	@InjectMocks
	private TaskForgeOrganizationService organizationService;
	
	@Test
	public void shouldCreateOrganizationIfNameIsUnique(){
		//Given
		String uniqueName = "Ajileye and Sons";
		Organization organization = Organization.builder().name(uniqueName).build();
		when(organizationRepository.existsByName(uniqueName)).thenReturn(false);
		when(organizationRepository.save(organization)).thenReturn(organization);
		
		//When
		OrganizationResponse response = organizationService.createNew(OrganizationRequest.builder().name(uniqueName).build());
		
		//Then
		assertNotNull(response);
		assertEquals(uniqueName, response.getName());
		verify(organizationRepository).save(organization);
		
	}
}