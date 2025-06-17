package app.bola.taskforge.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import app.bola.taskforge.domain.entity.Organization;
import org.hibernate.validator.constraints.URL;

/**
 * Request DTO for {@link Organization} entity
 */

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationRequest {
	
	@NotBlank(message = "Name is required")
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
	private String name;
	
	@NotBlank(message = "Slug is required")
	@Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and dashes")
	@Size(max = 50, message = "Slug must be 50 characters or fewer")
	private String slug;
	
	String description;
	
	@NotBlank(message = "Industry is required")
	@Size(max = 100, message = "Industry must be 100 characters or fewer")
	private String industry;
	
	@NotBlank(message = "Country is required")
	@Size(max = 100, message = "Country must be 100 characters or fewer")
	private String country;
	
	@Email(message = "Contact email must be valid")
	@NotBlank(message = "Contact email is required")
	private String contactEmail;
	
	@Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Contact phone must be a valid international number")
	private String contactPhone;
	
	@NotBlank(message = "Time zone is required")
	@Pattern(regexp = "^[A-Za-z/_]+$", message = "Time zone format is invalid (e.g., Africa/Lagos)")
	private String timeZone;
	
	@Size(max = 255)
	@URL(message = "Website URL must be valid")
	String websiteUrl;
	
	@Size(max = 255)
	@URL(message = "Website URL must be valid")
	String logoUrl;
}
