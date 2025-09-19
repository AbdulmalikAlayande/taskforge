package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference extends BaseEntity {

	@Builder.Default
	private boolean allowNotification = Boolean.TRUE;
	@Builder.Default
	private boolean allowInApp = Boolean.TRUE;
	@Builder.Default
	private boolean allowEmail = Boolean.FALSE;
	private LocalTime quietHoursStart;
	private LocalTime quietHoursEnd;
	
	@OneToOne(mappedBy = "notificationPreference")
	private Member member;
	
	public boolean isInQuietHours(LocalTime currentTime) {
		if (quietHoursStart == null || quietHoursEnd == null) {
			return false;
		}
		return currentTime.isAfter(quietHoursStart) && currentTime.isBefore(quietHoursEnd);
	}
}
