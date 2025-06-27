package app.bola.taskforge.notification.channel;

import app.bola.taskforge.notification.model.ChannelType;
import app.bola.taskforge.notification.model.DeliveryResult;
import app.bola.taskforge.notification.model.NotificationBundle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
	
	@Value("${app.brevo.api-key}")
	private String mailApiKey;
	
	@Value("${app.brevo.api-url}")
	private String mailClientProviderUrl;
	
	
	EmailChannelHandler(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
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
		return bundle.getChannels().contains(ChannelType.EMAIL) &&
				       bundle.getEmailTo() != null && !bundle.getEmailTo().isEmpty();
	}
	
	
	@Override
	public CompletableFuture<DeliveryResult> deliverAsync(NotificationBundle bundle) {
		return ChannelHandler.super.deliverAsync(bundle);
	}
	
	@Override
	public DeliveryResult deliver(NotificationBundle bundle) {
		try {
			EmailRequestObject emailObject = EmailRequestObject.builder()
				.subject(bundle.getTitle())
				.textContent(bundle.getMessage())
				.htmlContent(bundle.getHtmlMessage())
				.sender(new EmailRequestObject.Sender("noreply@taskforge.com", "TaskForge"))
				.to(Collections.singletonList(new EmailRequestObject.Recipient(bundle.getEmailTo(), bundle.getUserId())))
				.build();
			
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
	
	@Builder
	private static class EmailRequestObject {
		
		private String subject;
		private String textContent;
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
	@Builder
	private static class EmailResponseObject {
		private String messageId;
		private String code;
		private String error;
		private String message;
	}
}
