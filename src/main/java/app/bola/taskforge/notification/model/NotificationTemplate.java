package app.bola.taskforge.notification.model;


import app.bola.taskforge.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "channel"})})
public class NotificationTemplate extends BaseEntity {
	
	@Lob
	private String body;
	private String name;
	private String channel;
	private String subject;
	private String language;
	@Builder.Default
	private boolean active = Boolean.TRUE;
}
