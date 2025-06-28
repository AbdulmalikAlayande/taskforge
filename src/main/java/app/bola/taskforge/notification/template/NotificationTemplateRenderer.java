package app.bola.taskforge.notification.template;

import java.util.Locale;
import java.util.Map;

public interface NotificationTemplateRenderer {
	
	/*
	 * Render a notification template with the given name and channel.
	 *
	*/
	String render(String templateName, String channel, Map<String, Object> variables);
	
	
	String render(String templateName, String channel, Map<String, Object> variables, Locale locale);
}
