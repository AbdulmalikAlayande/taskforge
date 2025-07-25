package app.bola.taskforge.service.dto;

import app.bola.taskforge.domain.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
	String email;
	String phone;
	String publicId;
	String description;
	String logoUrl;
	String websiteUrl;
	LocalDateTime createdAt;
	LocalDateTime lastModifiedAt;
	Set<MemberResponse> members;
	Set<ProjectResponse> projects;
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				       .append("name", name)
				       .append("slug", slug)
				       .append("industry", industry)
				       .append("country", country)
				       .append("timeZone", timeZone)
				       .append("contactEmail", email)
				       .append("contactPhone", phone)
				       .append("publicId", publicId)
				       .append("description", description)
				       .append("logoUrl", logoUrl)
				       .append("websiteUrl", websiteUrl)
				       .append("createdAt", createdAt)
				       .append("lastModifiedAt", lastModifiedAt)
				       .append("members", members)
				       .append("projects", projects)
				       .toString();
	}
}
