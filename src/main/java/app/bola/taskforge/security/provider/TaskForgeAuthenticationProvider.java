package app.bola.taskforge.security.provider;

import app.bola.taskforge.exception.AuthenticationFailedException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@AllArgsConstructor
public class TaskForgeAuthenticationProvider implements AuthenticationProvider {
	
	final UserDetailsService userDetailsService;
	final BCryptPasswordEncoder passwordEncoder;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		System.out.println("Authentication Provider auth initiated.");
		
		String email = authentication.getPrincipal().toString();
		String password = authentication.getCredentials().toString();
		System.out.printf("Authentication Details: [%s] | [%s]\n", email, password);
		UserDetails userDetails = userDetailsService.loadUserByUsername(email);
		System.out.println("User Details"+userDetails);
		if (userDetails == null) {
			System.out.println("User Details is null");
			throw new AuthenticationFailedException("Invalid Credentials");
		}
		
		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			throw new BadCredentialsException("Invalid Password");
		}
		
		return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
