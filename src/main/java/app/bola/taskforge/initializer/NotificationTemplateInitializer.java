package app.bola.taskforge.initializer;

import app.bola.taskforge.notification.model.NotificationTemplate;
import app.bola.taskforge.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationTemplateInitializer implements CommandLineRunner {
	
	private final NotificationTemplateRepository templateRepo;
	
	Map<String, String> taskPushNotifications = Map.of(
			// TASK_DUE
			"TASK_DUE", """
			⏰ Deadline approaching!
			"${taskTitle}" is due ${dueDate}.
			Priority: ${priority}
			Project: ${projectName}
			""",
			
			// TASK_CREATED
			"TASK_CREATED",
			"""
			📝New task created
			${taskTitle} was added to ${projectName} by ${creatorName}.
			"Due: ${dueDate}
			""",
			
			// TASK_UPDATED
			"TASK_UPDATED",
			"""
			🔄 Task updated
			"${taskTitle}" was modified by ${editorName}.
			Changes: ${changeSummary}
			View updates: ${taskLink}
			""",
			
			// TASK_DELETED
			"TASK_DELETED",
			"""
			🗑️ Task deleted${taskTitle} was removed from ${projectName} by ${deleterName}.
			Reason: ${deletionReason}
			""",
			
			// TASK_ASSIGNED
			"TASK_ASSIGNED",
			"""
			👤 New assignment
			You've been assigned to ${taskTitle} by ${assignerName}.
			Due: ${dueDate}
			Priority: ${priority}
			Click to view → ${taskLink}
			""",
			
			// TASK_COMPLETED
			"TASK_COMPLETED",
			"""
			✅ Task completed!
			${taskTitle} was marked done by ${completerName}.
			${celebratoryEmoji} ${completionNotes}
			"""
	);
	
	Map<String, String> projectPushNotifications = Map.of(
			// PROJECT_CREATED
			"PROJECT_CREATED",
			"""
			🚀 New project launched!
			${projectName} was created by ${creatorName}.
			"Deadline: ${endDate}
			Team: ${memberCount} members
			Details → ${projectLink}
			""",
			
			// PROJECT_UPDATED
			"PROJECT_UPDATED",
			"""
			🔧 Project updated
			${projectName} was modified by ${editorName}.
			Key changes:
			\t- ${change1}
			\t- ${change2}
			View all → ${projectLink}
			""",
			
			// PROJECT_DELETED
			"PROJECT_DELETED",
			"""
			⚠️ Project archived
			${projectName} was archived by ${deleterName}.
			All tasks moved to ${archiveLocation}. Reason: ${deletionReason}
			""",
			
			// PROJECT_COMPLETED
			"PROJECT_COMPLETED",
			"""
			🏆 Project completed!
			${projectName} was finished ${daysAhead} ahead of schedule!
			Stats:
			\t- ${completedTasks} tasks
			\t- ${teamMembers} contributors
			View report → ${projectLink}
			"""
	);
	
	@Override
	public void run(String... args) {
		initializePushNotificationTemplates();
	}
	
	void initializePushNotificationTemplates() {
		List<NotificationTemplate> allPushTemplates = new ArrayList<>();
		allPushTemplates.addAll(initializeTaskPushNotificationTemplates());
		allPushTemplates.addAll(initializeProjectPushNotificationTemplates());
		
		List<NotificationTemplate> savedTemplates = allPushTemplates.stream()
                .filter(template -> !templateRepo.existsByName(template.getName()))
				.map(templateRepo::save)
                .toList();
		
		log.info("Saved {} push notification templates", savedTemplates.size());
	}
	
	private List<NotificationTemplate> initializeTaskPushNotificationTemplates() {
		
		String[] templateNames = {"TASK_DUE", "TASK_CREATED", "TASK_ASSIGNED", "TASK_COMPLETED", "TASK_UPDATED"};
		return Stream.of(templateNames).map(templateName -> (NotificationTemplate) NotificationTemplate.builder()
			.channel("push")
			.language("en")
			.body(taskPushNotifications.get(templateName))
			.name(templateName.toLowerCase(Locale.of("en")).replace('_', '-'))
			.build()
		).toList();
	}
	
	private List<NotificationTemplate> initializeProjectPushNotificationTemplates() {
		
		String[] templateNames = {"PROJECT_CREATED, PROJECT_UPDATED, PROJECT_DELETED, PROJECT_COMPLETED"};
		return Stream.of(templateNames).map(templateName -> (NotificationTemplate) NotificationTemplate.builder()
			.channel("push")
			.language("en")
			.body(projectPushNotifications.get(templateName))
			.name(templateName.toLowerCase(Locale.of("en")).replace('_', '-'))
			.build()
		).toList();
	}
}
