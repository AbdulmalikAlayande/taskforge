package app.bola.taskforge.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import app.bola.taskforge.domain.entity.Organization;
/**
 * Request DTO for {@link Organization} entity
 */

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationRequest {
	
	String name;
	String slug;
	String industry;
	String country;
	String contactEmail;
	String contactPhone;
	String timeZone;
	
	
}
