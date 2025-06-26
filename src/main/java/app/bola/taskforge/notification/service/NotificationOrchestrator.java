package app.bola.taskforge.notification.service;

import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.event.AppEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class NotificationOrchestrator {

	final DebounceEngine debounceEngine;
	final PreferenceManager preferenceManager;
	
	@EventListener
	public void handleEvent(AppEvent event) {
		
		List<Member> notificationCandidates = createNotificationCandidates(event);
		List<Member> preferenceFilteredCandidates = preferenceManager.filterPreferences(notificationCandidates);
		
		preferenceFilteredCandidates.forEach(debounceEngine::submit);
	}
	
	private List<Member> createNotificationCandidates(AppEvent event) {
		return null;
	}
}
