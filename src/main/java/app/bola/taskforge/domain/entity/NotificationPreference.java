package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference extends BaseEntity {

	private boolean allowInaApp = Boolean.TRUE;
	private boolean allowEmail = Boolean.FALSE;
	private LocalTime quietHoursStart;
	private LocalTime quietHoursEnd;
}
