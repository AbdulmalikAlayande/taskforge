package app.bola.taskforge.notification;

import app.bola.taskforge.domain.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Component
public class MailSender {
	
	
	private static final String API_KEY = "api-key";
	private final String apiKey;
	private final RestTemplate restTemplate;
	public static final String brevoUrl = "https://api.brevo.com/v3/smtp/email";
	
	
	public MailSender(@Value("${app.brevo.api-key}") String apiKey,
	                  RestTemplate restTemplate) {
		this.apiKey = apiKey;
		this.restTemplate = restTemplate;
	}
	
	/**
	 * Sends an email using the Brevo API.
	 *
	 * @return ResponseEntity with the result of the email sending operation.
	*/
	public ResponseEntity<?> sendEmail(List<Notification.Recipient> members, String subject, String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(API_KEY, apiKey);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		
		HttpEntity<Notification> requestEntity =
				new HttpEntity<>(buildNotificationObject(members, subject, body), headers);
		
		ResponseEntity<Notification> response = restTemplate.postForEntity(brevoUrl, requestEntity, Notification.class);
		
		if (response.getBody() != null) {
			if (!response.getStatusCode().is2xxSuccessful()) {
				return ResponseEntity.status(response.getStatusCode())
						       .body("Failed to send email: " + response.getBody());
			}
			else {
				return ResponseEntity.ok(app.bola.taskforge.domain.entity.Notification.builder()
						                         .title(Objects.requireNonNull(response.getBody()).getSubject())
						                         .body(Objects.requireNonNull(response.getBody()).getTextContent())
						                         .type(NotificationType.MEMBER_INVITED)
						                         .build());
			}
		}
		return ResponseEntity.status(500).body("Failed to send email: No response body");
	}
	
	private Notification buildNotificationObject(List<Notification.Recipient> recipients, String subject, String body) {
		

		Notification.Sender sender = new Notification.Sender("noreply@taskforge.com", "TaskForge Team");
		return new Notification(subject, body, sender, recipients);
	}
	
	@Getter
	@AllArgsConstructor
	public static class Notification {
		private String subject;
		private String textContent;
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
