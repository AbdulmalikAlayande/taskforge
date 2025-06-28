package app.bola.taskforge.notification.template;

import app.bola.taskforge.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class NotificationTemplateRender {
	
	final Context context;
	final MessageSource messageSource;
	final NotificationTemplateRepository templateRepository;
	
	public String render() {
		templateRepository.findByNameAndChannel("", "");
		return "";
	}
}
