package app.bola.taskforge.notification.service;

import app.bola.taskforge.domain.entity.NotificationPreference;
import app.bola.taskforge.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PreferenceManager {
	
	private final NotificationPreferenceRepository preferenceRepository;
	
	
	public NotificationPreference getPreference(String userId) {
		Optional<NotificationPreference> optionalPreference = preferenceRepository.findByUserId(userId);
		System.out.println("Fetched preference for userId " + userId + ": " + optionalPreference.get());
		return optionalPreference.orElse(null);
	}
}
