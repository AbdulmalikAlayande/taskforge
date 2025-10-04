package app.bola.taskforge.security.service;

import app.bola.taskforge.service.dto.OAuthUserInfo;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class GoogleOAuthVerifier implements OAuthVerifier {
	
	final OAuth2AuthorizedClientService clientService;

	@Override
	public OAuthUserInfo verifyToken(String accessToken) {
		return null;
	}

	@Override
	public String getProviderName() {
		return "Google";
	}
}
