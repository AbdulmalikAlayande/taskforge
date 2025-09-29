package app.bola.taskforge.security.handler;

import app.bola.taskforge.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OauthLoginSuccessHandler implements AuthenticationSuccessHandler {
	
	
	private final UserRepository userRepository;
	
	public OauthLoginSuccessHandler(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
		String userName = authToken.getPrincipal().getAttribute("name");
		OAuth2User oAuth2User = authToken.getPrincipal();
		
//		userRepository.findByEmail()
	}
}
