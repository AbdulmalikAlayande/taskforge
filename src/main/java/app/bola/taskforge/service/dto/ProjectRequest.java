package app.bola.taskforge.service.dto;


import app.bola.taskforge.domain.enums.ProjectCategory;
import app.bola.taskforge.domain.enums.ProjectPriority;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectRequest implements Serializable {
	
	String status;
	@NotBlank
	String name;
	@NotBlank
	String description;
	@NotNull
	LocalDate endDate;
	@NotNull
	LocalDate startDate;
	@NotBlank
	String organizationId;
	String teamLeadId;
	List<String> memberIds;
	@NotNull
	ProjectCategory category;
	@NotNull
	ProjectPriority priority;


	@Override
	public String toString() {
		return new java.util.StringJoiner(", ", ProjectRequest.class.getSimpleName() + "[", "]")
				.add("status='" + status + "'")
				.add("name='" + name + "'")
				.add("description='" + description + "'")
				.add("endDate=" + endDate)
				.add("startDate=" + startDate)
				.add("organizationId='" + organizationId + "'")
				.add("memberIds=" + memberIds)
				.add("category=" + category)
				.add("priority=" + priority)
				.toString();
	}
}
