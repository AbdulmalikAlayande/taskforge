package app.bola.taskforge.notification.template;

import app.bola.taskforge.notification.model.NotificationTemplate;
import app.bola.taskforge.notification.repository.NotificationTemplateRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;
import java.util.Map;

@Component
public class EmailNotificationTemplateRenderer implements NotificationTemplateRenderer {
	
	final Context context;
	final MessageSource messageSource;
	final SpringTemplateEngine templateEngine;
	final NotificationTemplateRepository templateRepository;
	
	public EmailNotificationTemplateRenderer(Context context, MessageSource messageSource, SpringTemplateEngine templateEngine,
	                                         NotificationTemplateRepository templateRepository) {
		this.context = context;
		this.messageSource = messageSource;
		this.templateEngine = templateEngine;
		this.templateRepository = templateRepository;
	}
	
	@Override
	public String render(String templateName, String channel, Map<String, Object> variables) {
		
		NotificationTemplate template = templateRepository.findByNameAndChannel(templateName, channel).orElse(null);
		if (template == null) {
			return render(templateName, variables);
		}
		variables.put("contentFragment", "email-fragments :: " + template.getName());
		context.setVariables(variables);
		return templateEngine.process("base-event", context);
	}
	
	
	public String render(String templateName, Map<String, Object> variables) {
		NotificationTemplate template = templateRepository.findByName(templateName).orElse(null);
		if (template == null) {
			return "";
		}
		context.setVariables(variables);
		return templateEngine.process(template.getBody(), context);
	}
	
	@Override
	public String render(String templateName, String channel, Map<String, Object> variables, Locale locale) {
		NotificationTemplate template = templateRepository.findByNameAndChannel(templateName, channel).orElse(null);
		if (template == null) {
			return "";
		}
		variables.forEach((key, value) -> {
			if (value instanceof String) {
				String message = messageSource.getMessage((String) value, null, (String) value, locale);
				context.setVariable(key, message);
			} else {
				context.setVariable(key, value);
			}
		});
		context.setLocale(locale);
		return templateEngine.process(template.getBody(), context);
	}
}
