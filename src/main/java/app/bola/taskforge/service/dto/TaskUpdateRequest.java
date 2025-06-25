package app.bola.taskforge.service.dto;

import app.bola.taskforge.domain.enums.TaskCategory;
import app.bola.taskforge.domain.enums.TaskPriority;

import java.time.LocalDate;

public record TaskUpdateRequest(String title, String description, LocalDate dueDate, LocalDate startDate,
                                TaskCategory category, TaskPriority priority) {
	
}
