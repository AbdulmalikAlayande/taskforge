package app.bola.taskforge.security.service;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

public class GoogleOAuthVerifier implements OAuthVerifier {
	
	OAuth2AuthorizedClientService clientService;
}
