package app.bola.taskforge.service.dto;

import app.bola.taskforge.domain.entity.Task;
import app.bola.taskforge.domain.enums.TaskCategory;
import app.bola.taskforge.domain.enums.TaskPriority;
import app.bola.taskforge.domain.enums.TaskStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for {@link Task} operations
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {
	
	boolean pinned;
	
	String title;
	String publicId;
	String projectId;
	String assigneeId;
	MemberResponse assignee;
	String description;
	String organizationId;
	
	TaskStatus status;
	TaskPriority priority;
	TaskCategory category;
	
	LocalDate dueDate;
	LocalDate startDate;
	
	LocalDateTime createdAt;
	LocalDateTime lastModifiedAt;
	LocalDateTime completedAt;
}
