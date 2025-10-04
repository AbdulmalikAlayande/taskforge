package app.bola.taskforge.security.service;

import app.bola.taskforge.service.dto.OAuthUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GitHubOAuthVerifier implements OAuthVerifier {
	
	@Override
	public OAuthUserInfo verifyToken(String accessToken) {
		return null;
	}
	
	@Override
	public String getProviderName() {
		return "Github";
	}
}

