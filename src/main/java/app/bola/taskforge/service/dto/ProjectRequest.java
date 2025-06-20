package app.bola.taskforge.service.dto;


import app.bola.taskforge.domain.enums.ProjectCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
	@NotEmpty
	List<String> memberIds;
	@NotNull
	ProjectCategory category;
}
