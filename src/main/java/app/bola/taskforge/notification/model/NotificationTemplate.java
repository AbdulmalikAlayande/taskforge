package app.bola.taskforge.notification.model;


import app.bola.taskforge.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class NotificationTemplate extends BaseEntity {
	
	private String name;
	private String channel;
	private String subject;
	private String language;
	@Lob
	private String body;
	private boolean active;
}
