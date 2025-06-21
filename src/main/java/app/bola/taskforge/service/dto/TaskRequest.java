package app.bola.taskforge.service.dto;

import app.bola.taskforge.domain.enums.TaskCategory;
import app.bola.taskforge.domain.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import app.bola.taskforge.domain.entity.Task;

import java.time.LocalDate;

/**
 * Represents a request to create or update a {@link Task}.
 * This class is used to encapsulate the data required for {@link Task} operations.
 */

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
	
	@NotBlank
	String title;
	String description;
	@NotBlank
	String projectId;
	@NotNull
	LocalDate dueDate;
	@NotNull
	LocalDate startDate;
	@NotBlank
	String organizationId;
	@NotNull
	TaskCategory category;
	@NotNull
	TaskPriority priority;
	
}
