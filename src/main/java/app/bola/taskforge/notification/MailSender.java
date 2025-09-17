package app.bola.taskforge.notification;

import app.bola.taskforge.domain.entity.Invitation;
import app.bola.taskforge.domain.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class MailSender {
	
	
	private static final String API_KEY = "api-key";
	private final String apiKey;
	private final RestTemplate restTemplate;
	private final Context context;
	private final TemplateEngine templateEngine;
	public static final String brevoUrl = "https://api.brevo.com/v3/smtp/email";
	
	
	public MailSender(@Value("${app.brevo.api-key}") String apiKey,
	                  RestTemplate restTemplate, Context context, TemplateEngine templateEngine) {
		this.apiKey = apiKey;
		this.restTemplate = restTemplate;
		this.context = context;
		this.templateEngine = templateEngine;
	}
	
	@NonNull
	private HttpHeaders getHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set(API_KEY, apiKey);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		return headers;
	}
	
	public ResponseEntity<?> sendWelcomeEmail(String username, String email, String organizationName) {
		Map<String, Object> contextVariables = Map.of(
			"username", username,
			"organizationName", organizationName,
			"facebookUrl", "https://www.facebook.com/TaskForgeApp",
			"twitterUrl", "https://twitter.com/TaskForgeApp",
			"instagramUrl", "https://www.instagram.com/TaskForgeApp",
			"organizationLogoUrl", "https://taskforge.s3.amazonaws.com/logo.png",
			"dashboardUrl", "https://app.taskforge.com/dashboard"
		);
		context.setVariables(contextVariables);
		String htmlContent = templateEngine.process("admin-welcome", context);
		return sendEmail("Welcome to TaskForge", htmlContent, List.of(new Notification.Recipient(email, username)));
	}
	
	public ResponseEntity<?> sendInvitationMail(Invitation invitation, String organizationName){
		String inviteeName = StringUtils.isNotBlank(invitation.getInviteeName()) ?
				                     invitation.getInviteeName() : invitation.getEmail().split("@")[0];
		Map<String, Object> contextVariables = Map.of(
			"inviteeName", inviteeName,
			"inviterName", invitation.getInvitedBy().getFirstName()+" "+invitation.getInvitedBy().getLastName(),
			"inviteeEmail", invitation.getEmail(),
			"invitationLink", invitation.getInvitationLink(),
			"organizationName", organizationName
		);
		context.setVariables(contextVariables);
		String htmlContent = templateEngine.process("member-invitation", context);
		return sendEmail(String.format("Invitation to join %s on TaskForge", organizationName), htmlContent,
				List.of(new Notification.Recipient(invitation.getEmail(), inviteeName)));
	}
	
	public ResponseEntity<?> sendEmail(String subject, String content, List<Notification.Recipient> recipients) {
		HttpHeaders headers = getHttpHeaders();
		Notification.Sender sender = new Notification.Sender("noreply@taskforge.com", "TaskForge Team");
		Notification notification = new Notification(subject, content, sender, recipients);
		HttpEntity<Notification> requestEntity = new HttpEntity<>(notification, headers);
		
		ResponseEntity<Notification> response = restTemplate.postForEntity(brevoUrl, requestEntity, Notification.class);
		if (response.getBody() != null && response.getStatusCode().is2xxSuccessful()) {
			return ResponseEntity.status(HttpStatus.OK).body(response.getBody());
		}
		else if (response.getBody() != null && !response.getStatusCode().is2xxSuccessful()){
			return ResponseEntity.status(500).body("Failed to send email");
		}
		return ResponseEntity.status(500).body("Failed to send email: No response body");
	}
	
	
	public ResponseEntity<?> sendEmail(List<Notification.Recipient> members, String subject, String body) {
		HttpHeaders headers = getHttpHeaders();
		
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
						                         .body(Objects.requireNonNull(response.getBody()).getHtmlContent())
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
