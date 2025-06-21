package app.bola.taskforge.service.dto;

import app.bola.taskforge.domain.enums.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectResponse implements Serializable {
	
	String name;
	ProjectStatus status;
	String category;
	String publicId;
	boolean archived;
	String createdBy;
	String modifiedBy;
	String description;
	LocalDate endDate;
	LocalDate startDate;
	LocalDateTime createdAt;
	LocalDateTime lastModifiedAt;
	Set<MemberResponse> members;
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				       .append("name", name)
				       .append("category", category)
				       .append("publicId", publicId)
				       .append("archived", archived)
				       .append("createdBy", createdBy)
				       .append("modifiedBy", modifiedBy)
				       .append("description", description)
				       .append("endDate", endDate)
				       .append("startDate", startDate)
				       .append("createdAt", createdAt)
				       .append("lastModifiedAt", lastModifiedAt)
				       .append("members", members)
				       .toString();
	}
}
