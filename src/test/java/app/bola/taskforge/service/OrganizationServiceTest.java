package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.exception.TaskForgeException;
import app.bola.taskforge.repository.OrganizationRepository;
import app.bola.taskforge.service.dto.OrganizationRequest;
import app.bola.taskforge.service.dto.OrganizationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(value = MockitoExtension.class)
class OrganizationServiceTest {
	
	@Mock
	private ModelMapper modelMapper;
	@Mock
	private OrganizationRepository organizationRepository;
	@InjectMocks
	private TaskForgeOrganizationService organizationService;
	
	@BeforeEach
	public void setUp() {
		// This method can be used to set up common test data or configurations if needed
	}
	
	@Test
	@DisplayName("should create organization if name is unique")
	public void shouldCreateOrganizationIfNameIsUnique(){
		//Given
		String uniqueName = "Ajileye and Sons";
		Organization organization = Organization.builder().name(uniqueName).build();
		when(organizationRepository.existsByName(uniqueName)).thenReturn(false);
		when(modelMapper.map(any(OrganizationRequest.class), eq(Organization.class))).thenReturn(organization);
		when(organizationRepository.save(any())).thenReturn(organization);
		when(modelMapper.map(any(Organization.class), eq(OrganizationResponse.class))).thenReturn(
			OrganizationResponse.builder().name(uniqueName).build()
		);
		
		//When
		OrganizationResponse response = organizationService.createNew(OrganizationRequest.builder().name(uniqueName).build());
		
		//Then
		assertNotNull(response);
		assertEquals(uniqueName, response.getName());
		verify(organizationRepository).save(organization);
	}
	
	@Test
	@DisplayName("should throw exception if organization with name already exists")
	public void shouldThrowExceptionIfOrganizationWithNameAlreadyExists() {
		//Given
		String existingName = "Ajileye and Sons";
		when(organizationRepository.existsByName(existingName)).thenReturn(true);
		
		//When & Then
		TaskForgeException exception = assertThrows(TaskForgeException.class, () -> organizationService.createNew(
			OrganizationRequest.builder()
				.name(existingName)
				.slug("ajileye-and-sons")
				.industry("Technology")
				.country("Nigeria")
				.timeZone("Africa/Lagos")
				.build())
		);
		
		assertEquals("Organization with this name already exists", exception.getMessage());
		verify(organizationRepository).existsByName(existingName);
	}
	
	@Test
	public void shouldMapOrganizationToResponse() {
		// Given
		Organization organization = Organization.builder()
				                     .name("eReach Org")
				                     .slug("e-reach-org")
				                     .country("Nigeria")
				                     .industry("HealthCare")
				                     .timeZone("Africa/Lagos")
				                     .contactEmail("alaabdulmalik03@gmail.com")
				                     .contactPhone("+2348034567890")
				                     .build();
		
		// When
		when(modelMapper.map(organization, OrganizationResponse.class))
				.thenReturn(OrganizationResponse.builder()
                   .name("eReach Org")
                   .slug("e-reach-org")
                   .country("Nigeria")
                   .industry("HealthCare")
                   .timeZone("Africa/Lagos")
                   .contactEmail("alaabdulmalik03@gmail.com")
                   .contactPhone("+2348034567890")
                   .build()
				);
		
		OrganizationResponse response = organizationService.toResponse(organization);
		
		// Then
		assertNotNull(response);
		assertEquals("eReach Org", response.getName());
		assertThat(response).hasNoNullFieldsOrPropertiesExcept(
			"publicId", "createdAt", "projects",
			"lastModifiedAt", "members"
		);
		verify(modelMapper).map(organization, OrganizationResponse.class);
	}
	
	@Test
	public void shouldThrowInvalidRequestExceptionIfRequiredFieldsAreMissing() {
		// Given
		
	}
	
	@Test
	public void shouldThrowInvalidRequestExceptionIfFieldsBreakValidationRules() {
	
	}
}