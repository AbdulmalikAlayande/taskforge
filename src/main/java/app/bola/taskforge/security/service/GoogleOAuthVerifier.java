package app.bola.taskforge.security.service;

import app.bola.taskforge.service.dto.OAuthUserInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

@Slf4j
@Component
@AllArgsConstructor
public class GoogleOAuthVerifier implements OAuthVerifier {
	
	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String clientId;
	@Value("${spring.security.oauth2.client.registration.google.client-secret}")
	private String clientSecret;
	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();
	final OAuth2AuthorizedClientService clientService;


	@Override
	public OAuthUserInfo verifyToken(String accessToken) {
		try {
			String tokenInfoUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + accessToken;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<>(headers);
				
			ResponseEntity<String> response = restTemplate.exchange(
				tokenInfoUrl, HttpMethod.GET,
				entity, String.class
			);
			if (response.getStatusCode() == HttpStatus.OK) {
				JsonNode jsonNode = objectMapper.readTree(response.getBody());
				String audience = jsonNode.path("aud").asText();
				if (!clientId.equals(audience)) {
					log.warn("Token audience mismatch. Expected: {}, Got: {}", clientId, audience);
					return null;
				}
				return getUserInfo(accessToken);
			} 
			return null;
		} catch (JsonProcessingException e) {
			log.error("Error parsing Google token info response", e);
			throw new RuntimeException("Failed to parse token info response");
		}

	}

	private OAuthUserInfo getUserInfo(String accessToken) {
		try {
			String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<>(headers);
				
			ResponseEntity<String> response = restTemplate.exchange(
				userInfoUrl, HttpMethod.GET,
				entity, String.class
			);
			if (response.getStatusCode() == HttpStatus.OK) {
				JsonNode jsonNode = objectMapper.readTree(response.getBody());
				OAuthUserInfo userInfo = new OAuthUserInfo();
				userInfo.setEmail(jsonNode.path("email").asText());
				userInfo.setName(jsonNode.path("name").asText());
				userInfo.setImageUrl(jsonNode.path("picture").asText());
				userInfo.setEmailVerified(jsonNode.path("verified_email").asBoolean());
				userInfo.setProviderId(jsonNode.path("id").asText());
				return userInfo;
			}
			return null;
		} catch (JsonProcessingException e) {
			log.error("Error parsing Google user info response", e);
			throw new RuntimeException("Failed to parse user info response");
		}
	}

	@Override
	public String getProviderName() {
		return "Google";
	}
}
