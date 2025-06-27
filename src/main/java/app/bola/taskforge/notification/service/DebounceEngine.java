package app.bola.taskforge.notification.service;

import app.bola.taskforge.domain.entity.NotificationPreference;
import app.bola.taskforge.notification.model.NotificationBundle;
import app.bola.taskforge.notification.model.NotificationCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DebounceEngine {
	
	final ChannelRouter channelRouter;
	final RedisTemplate<String, Object> redisTemplate;
	final ScheduledExecutorService scheduledExecutorService;
	private static final Duration DEBOUNCE_WINDOW = Duration.ofMinutes(10);
	private static final Set<String> scheduledKeys = ConcurrentHashMap.newKeySet();
	private final NotificationTemplate notificationTemplate;
	
	
	public void submit(NotificationCandidate candidate, NotificationPreference preference) {
		String debounceKey = candidate.getDebounceKey();
		
		redisTemplate.opsForList().rightPush(debounceKey, candidate);
		redisTemplate.expire(debounceKey, DEBOUNCE_WINDOW);
		
		if (scheduledKeys.add(debounceKey)) {
			scheduleProcessing(debounceKey, preference);
		};
	}
	
	public void scheduleProcessing(String debounceKey, NotificationPreference preference) {
		scheduledExecutorService.schedule(() -> {
			try {
				List<Object> objects = redisTemplate.opsForList().range(debounceKey, 0, -1);
			
				if (objects != null) {
					List<NotificationCandidate> candidates =
						objects.stream().map(obj -> (NotificationCandidate) obj).toList();
					
					NotificationBundle bundle = mergeNotifications(candidates);
					bundle.setEmailTo(preference.getMember().getEmail());
					
					channelRouter.route(bundle, preference);
					
					redisTemplate.delete(debounceKey);
				}
			}catch (Exception e) {
				throw new RuntimeException(e);
			}
		}, DEBOUNCE_WINDOW.toMillis(), TimeUnit.MILLISECONDS);
		
	}
	
	private NotificationBundle mergeNotifications(List<NotificationCandidate> candidates) {
		NotificationCandidate firstCandidate = candidates.getFirst();
		List<String> sourceEntityIds = candidates.stream().map(NotificationCandidate::getSourceEntityId).toList();
		return NotificationBundle.builder()
				       .userId(firstCandidate.getUserId())
				       .title(notificationTemplate.renderTitle(firstCandidate.getNotificationType(), candidates))
				       .message(notificationTemplate.renderMessage(firstCandidate.getNotificationType(), candidates))
				       .htmlMessage(notificationTemplate.renderHtmlMessage(firstCandidate.getNotificationType(), candidates))
				       .sourceEntityIds(sourceEntityIds)
				       .scheduledAt(Instant.now())
				       .build();
	}
	
}
