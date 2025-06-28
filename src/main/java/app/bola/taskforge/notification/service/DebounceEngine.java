package app.bola.taskforge.notification.service;

import app.bola.taskforge.domain.entity.NotificationPreference;
import app.bola.taskforge.notification.model.ChannelType;
import app.bola.taskforge.notification.model.NotificationBundle;
import app.bola.taskforge.notification.model.NotificationCandidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebounceEngine {
	
	final ChannelRouter channelRouter;
	final RedisTemplate<String, Object> redisTemplate;
	final ScheduledExecutorService scheduledExecutorService;
	@Value("#{T(java.time.Duration).parse('${app.notification.debounce-window:PT10M}')}")
	private Duration debounceWindow;
	private static final Set<String> scheduledKeys = ConcurrentHashMap.newKeySet();
	
	
	public void submit(Map<NotificationCandidate, NotificationPreference> preferenceMap) {
		preferenceMap.forEach((candidate, preference) -> {
			String debounceKey = candidate.getDebounceKey();
			
			redisTemplate.opsForList().rightPush(debounceKey, candidate);
			redisTemplate.expire(debounceKey, debounceWindow);
			
			if (scheduledKeys.add(debounceKey)) {
				scheduleProcessing(debounceKey, preference);
			} else {
				log.info("Debounce key {} is already scheduled, skipping rescheduling.", debounceKey);
			}
		});
	}
	
	private void scheduleProcessing(String debounceKey, NotificationPreference preference) {
		scheduledExecutorService.schedule(() -> {
			try {
				List<Object> objects = redisTemplate.opsForList().range(debounceKey, 0, -1);
				if (objects != null) {
					List<NotificationCandidate> candidates =
							objects.stream().map(obj -> (NotificationCandidate) obj).toList();
					
					NotificationBundle bundle = mergeNotifications(candidates, preference);
					
					channelRouter.route(bundle, preference);
					
					redisTemplate.delete(debounceKey);
				}
				
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				scheduledKeys.remove(debounceKey);
			}
		}, debounceWindow.toMillis(), TimeUnit.MILLISECONDS);
	}
	
	private NotificationBundle mergeNotifications(List<NotificationCandidate> candidates, NotificationPreference preference) {
		
		candidates.sort(Comparator.comparing(NotificationCandidate::getPriority));
		NotificationCandidate firstCandidate = candidates.getFirst();
		
		List<String> sourceEntityIds = candidates.stream().map(NotificationCandidate::getSourceEntityId).toList();
		List<String> sourceEntityTypes = candidates.stream().map(NotificationCandidate::getSourceEntityType).toList();
		
		if (firstCandidate != null) {
			return NotificationBundle.builder()
				.scheduledAt(Instant.now())
				.sourceEntityIds(sourceEntityIds)
				.userId(firstCandidate.getUserId())
				.sourceEntityTypes(sourceEntityTypes)
				.channels(getAllowedChannels(preference))
				.emailTo(preference.getMember().getEmail())
				.quietHours(new NotificationBundle.QuietHours(preference.getQuietHoursStart(), preference.getQuietHoursEnd()))
				.build();
		}
		
		log.warn("No candidates found for merging notifications, returning empty bundle.");
		return NotificationBundle.builder().build();
	}
	
	private Set<ChannelType> getAllowedChannels(NotificationPreference preference) {
		Set<ChannelType> allowedChannels = new HashSet<>();
		if (preference.isAllowEmail()) {
			allowedChannels.add(ChannelType.EMAIL);
		}
		if (preference.isAllowInApp()) {
			allowedChannels.add(ChannelType.WEBSOCKET);
			allowedChannels.add(ChannelType.PUSH);
		}
		return allowedChannels;
	}
}
