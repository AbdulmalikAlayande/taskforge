package app.bola.taskforge.security.service;

import app.bola.taskforge.service.dto.OAuthUserInfo;

public interface OAuthVerifier {
	
	OAuthUserInfo verifyToken(String accessToken);
	String getProviderName();
	
}
