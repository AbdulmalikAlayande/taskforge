package app.bola.taskforge.notification.service;

import app.bola.taskforge.event.EventType;
import app.bola.taskforge.notification.model.NotificationCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class NotificationTemplate {
	
	
	public String renderTitle(EventType eventType, List<NotificationCandidate> candidates) {
		final int size = candidates.size();
		
		return switch (eventType) {
			case TASK_DUE -> size > 1 ? String.format("%d tasks are due", size) : "1 task is due";
			case TASK_CREATED -> size > 1 ? String.format("%d tasks created", size) : "New task created";
			case TASK_UPDATED -> size > 1 ? String.format("%d tasks updated", size) : "Task updated";
			case TASK_DELETED -> "Task deleted";
			case TASK_ASSIGNED -> size > 1 ? String.format("%d new tasks assigned due", size) : "New task assigned";
			case TASK_COMPLETED -> size > 1 ? String.format("%d tasks completed", size) : "Task completed";
			default -> "A new event occurred";
		};
	}
	
	public String renderMessage(EventType notificationType, List<NotificationCandidate> candidates) {
		
		return switch (notificationType) {
			case TASK_ASSIGNED -> "You have been assigned a new task";
			case TASK_UPDATED -> candidates.size() > 1 ? String.format("You have %d task updates", candidates.size()) : "A task has been updated";
			case TASK_CREATED -> "A new task was created";
			default -> "You have a new notification";
		};
	}
	
	public String renderHtmlMessage(EventType type, List<NotificationCandidate> candidates) {
		return "<p>" + renderMessage(type, candidates) + "</p>";
	}
}
