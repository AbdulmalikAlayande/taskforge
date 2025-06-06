package app.bola.taskforge.service.dto;

import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.entity.Project;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for {@link Organization} entity.
 */

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationResponse {
	
	String name;
	String slug;
	String industry;
	String country;
	String timeZone;
	String contactEmail;
	String contactPhone;
	String publicId;
	LocalDateTime createdAt;
	LocalDateTime lastModifiedAt;
	Set<Member> members;
	Set<Project> projects;
}
