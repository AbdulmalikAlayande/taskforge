package app.bola.taskforge.security.service;

import app.bola.taskforge.service.dto.UserInfo;

public interface OAuthVerifier {
	
	UserInfo verifyToken(String accessToken);
	String getProviderName();
	
}
