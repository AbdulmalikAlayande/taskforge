package app.bola.taskforge.controller;

import app.bola.taskforge.service.OrganizationService;
import app.bola.taskforge.service.dto.OrganizationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@WebMvcTest(OrganizationController.class)
public class OrganizationControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private OrganizationService organizationService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	public void shouldCreateOrganizationIfNameIsUnique() throws Exception {
		
		// Given
		String uniqueName = "Ajileye and Sons";
		OrganizationRequest request = OrganizationRequest.builder()
				                              .name(uniqueName)
				                              .description("A family business")
				                              .timeZone("Africa/Lagos")
				                              .country("Nigeria")
				                              .industry("Industrial")
				                              .contactPhone("+2341234567890")
				                              .contactEmail("email@gmail.com")
				                              .logoUrl("https://example.com/logo.png")
				                              .websiteUrl("https://example.com")
				                              .build();
		
		// When
		mockMvc.perform(post("/api/organizations/create-new")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value(request.getName()))
				.andExpectAll(
					jsonPath("$.description").value(request.getDescription()),
					jsonPath("$.timeZone").value(request.getTimeZone()),
					jsonPath("$.country").value(request.getCountry()),
					jsonPath("$.industry").value(request.getIndustry()),
					jsonPath("$.contactPhone").value(request.getContactPhone()),
					jsonPath("$.contactEmail").value(request.getContactEmail()),
					jsonPath("$.logoUrl").value(request.getLogoUrl()),
					jsonPath("$.websiteUrl").value(request.getWebsiteUrl())
				);
		
		// Then
		verify(organizationService, times(1)).createNew(any());
	}
	
	@Test
	public void shouldReturn403IfNameIsNotUnique() throws Exception {
	
	}
	
	@Test
	public void shouldReturn400IfRequestIsInvalid() throws Exception {
	
	}
	
}
