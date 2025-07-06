package app.bola.taskforge.notification.repository;

import app.bola.taskforge.notification.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {
	
	/**
	 * Find a notification template by its name.
	 *
	 * @param name the name of the notification template
	 * @return the notification template if found, otherwise null
	 */
	Optional<NotificationTemplate> findByName(String name);

	/**
	 * Find a notification template by its name and channel.
	 *
	 * @param name   the name of the notification template
	 * @param channel the channel of the notification template
	 * @return the notification template if found, otherwise null
	 */
	Optional<NotificationTemplate> findByNameAndChannel(String name, String channel);
	
	boolean existsByName(String name);
}
