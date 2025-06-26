package app.bola.taskforge.notification.channel;

import app.bola.taskforge.notification.model.DeliveryResult;
import app.bola.taskforge.notification.model.NotificationBundle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
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

@Component
public class EmailChannelHandler implements ChannelHandler{
	
	private static final String API_KEY = "api-key";
	final RestTemplate restTemplate;
	final String mailApiKey;
	final String mailClientProviderUrl;
	private HttpHeaders httpHeaders;
	
	EmailChannelHandler(RestTemplate restTemplate, @Value("${app.brevo.api-key}") String mailApiKey,
	                    @Value("${app.brevo.api-url}")  String mailClientProviderUrl) {
		
		this.restTemplate = restTemplate;
		this.mailApiKey = mailApiKey;
		this.mailClientProviderUrl = mailClientProviderUrl;
		
		buildHttpHeaders();
	}
	
	private void buildHttpHeaders() {
		httpHeaders = new HttpHeaders();
		httpHeaders.set(API_KEY, mailApiKey);
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
	}
	
	@Override
	public CompletableFuture<DeliveryResult> deliverAsync(NotificationBundle bundle) {
		return ChannelHandler.super.deliverAsync(bundle);
	}
	
	@Override
	public DeliveryResult deliver(NotificationBundle bundle) {
		EmailObject emailObject = EmailObject.builder()
			.subject(bundle.getTitle())
			.textContent(bundle.getMessage())
			.htmlContent(bundle.getHtmlMessage())
			.sender(new EmailObject.Sender("noreply@taskforge.com", "TaskForge"))
			.to(Collections.singletonList(new EmailObject.Recipient(bundle.getEmailTo(), bundle.getUserId())))
			.build();
		
		HttpEntity<EmailObject> httpEntity = new HttpEntity<>(emailObject, httpHeaders);
		ResponseEntity<DeliveryResult> response = restTemplate.postForEntity(mailClientProviderUrl, httpEntity, DeliveryResult.class);
		
		
		return ChannelHandler.super.deliver(bundle);
	}
	
	@Builder
	private static class EmailObject {
		
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
}
