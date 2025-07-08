package app.bola.taskforge.notification.channel;

import app.bola.taskforge.notification.model.ChannelType;
import app.bola.taskforge.notification.model.DeliveryResult;
import app.bola.taskforge.notification.model.NotificationBundle;
import app.bola.taskforge.notification.template.NotificationTemplateRenderer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class EmailChannelHandler implements ChannelHandler{
	
	private static final String API_KEY = "api-key";
	private HttpHeaders httpHeaders;
	final RestTemplate restTemplate;
	final NotificationTemplateRenderer templateRender;
	@Value("${app.brevo.api-key}")
	private String mailApiKey;
	
	@Value("${app.brevo.api-url}")
	private String mailClientProviderUrl;
	
	
	public EmailChannelHandler(RestTemplate restTemplate, @Qualifier("emailNotificationTemplateRenderer") NotificationTemplateRenderer templateRender) {
		
		this.restTemplate = restTemplate;
		this.templateRender = templateRender;
		buildHttpHeaders();
	}
	
	private void buildHttpHeaders() {
		httpHeaders = new HttpHeaders();
		httpHeaders.set(API_KEY, mailApiKey);
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
	}
	
	@Override
	public ChannelType getChannelType() {
		return ChannelType.EMAIL;
	}
	
	@Override
	public boolean canHandle(NotificationBundle bundle) {
		return bundle.getChannels().contains(ChannelType.EMAIL) && StringUtils.isNotBlank(bundle.getEmailTo());
	}
	
	
	@Override
	public CompletableFuture<DeliveryResult> deliverAsync(NotificationBundle bundle) {
		return ChannelHandler.super.deliverAsync(bundle);
	}
	
	@Override
	public DeliveryResult deliver(NotificationBundle bundle) {
		
		String emailContent = templateRender.render(bundle.getTemplateName(), "email", bundle.getTemplateVariables());
		try {
			EmailRequestObject emailObject = new EmailRequestObject();
			emailObject.setSubject(bundle.getTitle());
			emailObject.setHtmlContent(emailContent);
			emailObject.setSender(new EmailRequestObject.Sender("noreply@taskforge.com", "TaskForge"));
			emailObject.setTo(Collections.singletonList(new EmailRequestObject.Recipient(bundle.getEmailTo(), bundle.getUserId())));
			HttpEntity<EmailRequestObject> httpEntity = new HttpEntity<>(emailObject, httpHeaders);
			
			ResponseEntity<EmailResponseObject> response =
					restTemplate.postForEntity(mailClientProviderUrl, httpEntity, EmailResponseObject.class);
			
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				return DeliveryResult.success(bundle.getId(), ChannelType.EMAIL, response.getBody().getMessageId());
			} else {
				return DeliveryResult.failure(bundle.getId(), ChannelType.EMAIL, "HTTP error: " + response.getStatusCode());
				
			}
		} catch (Exception exception) {
			log.error("Email delivery failed for bundle: {}", bundle.getId(), exception);
			return DeliveryResult.failure(bundle.getId(), ChannelType.EMAIL, exception.getMessage());
		}
	}
	
	@Getter
	@Setter
	private static class EmailRequestObject {
		
		private String subject;
		private String htmlContent;
		private Sender sender;
		private List<Recipient> to;
		
		@Getter
		@AllArgsConstructor
		public static class Recipient {
			private String email;
			private String name;
		}
		
		@Getter
		@AllArgsConstructor
		private static class Sender {
			private String email;
			private String name;
		}
	}
	
	@Getter
	@Setter
	private static class EmailResponseObject {
		private String messageId;
		private String code;
		private String error;
		private String message;
	}
}
