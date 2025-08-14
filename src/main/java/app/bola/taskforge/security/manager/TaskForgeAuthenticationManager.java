package app.bola.taskforge.security.manager;

import app.bola.taskforge.exception.AuthenticationFailedException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TaskForgeAuthenticationManager implements AuthenticationManager {
	
	final AuthenticationProvider authenticationProvider;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		System.out.println("Checking If Auth Provider Supports Authentication Type.");
		if (authenticationProvider.supports(authentication.getClass())) {
			System.out.println("Auth Provider Supported.");
			return authenticationProvider.authenticate(authentication);
		}
		System.out.println("Auth Provider Not Supported.");
		throw new AuthenticationFailedException("Authentication type not supported");
	}
	
	
}
