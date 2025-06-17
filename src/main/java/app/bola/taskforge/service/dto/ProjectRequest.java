package app.bola.taskforge.service.dto;


import app.bola.taskforge.domain.enums.ProjectCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
	
	String name;
	String description;
	LocalDate endDate;
	LocalDate startDate;
	String organizationId;
	String status;
	List<String> memberIds;
	ProjectCategory category;
}
