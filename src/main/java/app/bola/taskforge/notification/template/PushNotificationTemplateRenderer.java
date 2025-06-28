package app.bola.taskforge.notification.template;

import app.bola.taskforge.notification.model.NotificationTemplate;
import app.bola.taskforge.notification.repository.NotificationTemplateRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Locale;
import java.util.Map;

@Component
public class PushNotificationTemplateRenderer implements NotificationTemplateRenderer {
	
	final Context context;
	final MessageSource messageSource;
	final SpringTemplateEngine templateEngine;
	final NotificationTemplateRepository templateRepository;
	
	public PushNotificationTemplateRenderer(Context context, MessageSource messageSource, SpringTemplateEngine templateEngine,
	                                        NotificationTemplateRepository templateRepository) {
		this.context = context;
		this.messageSource = messageSource;
		this.templateEngine = templateEngine;
		this.templateRepository = templateRepository;
		
		StringTemplateResolver templateResolver = new StringTemplateResolver();
		templateResolver.setTemplateMode(TemplateMode.TEXT);
		templateResolver.setCacheable(true);
		
		templateEngine.setTemplateResolver(templateResolver);
	}
	
	@Override
	public String render(String templateName, String channel, Map<String, Object> variables) {
		NotificationTemplate template = templateRepository.findByNameAndChannel(templateName, channel).orElse(null);
		if (template == null) {
			return "";
		}
		
		context.setVariables(variables);
		return templateEngine.process(template.getName(), context);	}
	
	@Override
	public String render(String templateName, String channel, Map<String, Object> variables, Locale locale) {
		return "";
	}
}
