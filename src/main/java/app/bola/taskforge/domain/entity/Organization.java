package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Organization extends BaseEntity {
	
	@Column(unique = true, nullable = false)
	private String name;
	@Column(unique = true, nullable = false)
	private String slug;
	private String description;
	private String industry;
	private String country;
	private String timeZone;
	private String contactEmail;
	private String contactPhone;
	private String logoUrl;
	private String websiteUrl;
	
	@OneToMany(mappedBy = "organization")
	@Builder.Default
	private Set<Member> members = new HashSet<>();
	
	@OneToMany(mappedBy = "organization")
	@Builder.Default
	private Set<Project> projects = new HashSet<>();
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				       .append("id", getId())
				       .append("deleted", isDeleted())
				       .append("version", getVersion())
				       .append("publicId", getPublicId())
				       .append("createdAt", getCreatedAt())
				       .append("updatedAt", getLastModifiedAt())
				       .append("name", name)
				       .append("description", description)
				       .append("industry", industry)
				       .append("country", country)
				       .append("timeZone", timeZone)
				       .append("slug", slug)
				       .append("contactEmail", contactEmail)
				       .append("contactPhone", contactPhone)
				       .append("logoUrl", logoUrl)
				       .append("websiteUrl", websiteUrl)
				       .append("members", members)
				       .append("projects", projects)
				       .toString();
	}
}
