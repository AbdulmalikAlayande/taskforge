package app.bola.taskforge.security.service;

import app.bola.taskforge.service.dto.OAuthUserInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubOAuthVerifier implements OAuthVerifier {

	@Value("${spring.security.oauth2.client.registration.github.client-id}")
	private String clientId;
	@Value("${spring.security.oauth2.client.registration.github.client-secret}")
	private String clientSecret;
	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final OAuth2AuthorizedClientService clientService;

	
	@Override
	public OAuthUserInfo verifyToken(String accessToken) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);

			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(
				"https://api.github.com/user", HttpMethod.GET, entity, String.class
			);
			if (response.getStatusCode() == HttpStatus.OK) {
				JsonNode githubUser = objectMapper.readTree(response.getBody());
				OAuthUserInfo userInfo = new OAuthUserInfo();
				userInfo.setProviderId(githubUser.path("id").asText());
				userInfo.setImageUrl(githubUser.path("avatar_url").asText());
				userInfo.setName(githubUser.path("name").asText(null) != null ? githubUser.path("name").asText() : githubUser.path("login").asText());
				String email = githubUser.path("email").asText(null);
				userInfo.setEmail(StringUtils.isNotBlank(email) ? email : getPrimaryEmail(accessToken));
				userInfo.setEmailVerified(StringUtils.isNotBlank(email));
				return userInfo;
			}
			log.warn("GitHub token verification failed with status: {}", response.getStatusCode());
			return null;
		} catch (Exception e) {
			log.error("Error verifying GitHub token", e);
			throw new RuntimeException("Failed to verify GitHub token", e);
		}
	}
	
	private String getPrimaryEmail(String accessToken) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);
			headers.set("Accept", "application/vnd.github+json");
			headers.set("X-GitHub-Api-Version", "2022-11-28");
			
			HttpEntity<String> entity = new HttpEntity<>(headers);
			
			ResponseEntity<String> response = restTemplate.exchange(
				"https://api.github.com/user/emails", HttpMethod.GET, entity, String.class
			);

			if (response.getStatusCode() == HttpStatus.OK) {
				JsonNode emails = objectMapper.readTree(response.getBody());
				for (JsonNode emailObj : emails) {
					if (emailObj.path("primary").asBoolean() && emailObj.path("verified").asBoolean()) {
						return emailObj.path("email").asText();
					}
				}
				for (JsonNode emailObj : emails) {
					if (emailObj.path("verified").asBoolean()) {
						return emailObj.path("email").asText();
					}
				}
			}
			return null;
		} catch (Exception e) {
			log.error("Error getting GitHub primary email", e);	
			return null;
		}
	}

	@Override
	public String getProviderName() {
		return "Github";
	}
}

