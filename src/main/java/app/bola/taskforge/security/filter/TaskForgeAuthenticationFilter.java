package app.bola.taskforge.security.filter;

import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.exception.TaskForgeException;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.security.dto.LoginRequest;
import app.bola.taskforge.security.handler.TaskForgeAuthenticationFailureHandler;
import app.bola.taskforge.security.provider.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class TaskForgeAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	private final ObjectMapper objectMapper;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		try {
			
			LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
			String email = loginRequest.getEmail();
			String password = loginRequest.getPassword();
			
			if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
				throw new BadCredentialsException("Bad Credentials: Email and password must not be blank or empty");
			}
			
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
			Authentication authResult = authenticationManager.authenticate(authToken);
			SecurityContextHolder.getContext().setAuthentication(authResult);
			return authResult;
		} catch (IOException exception) {
			throw new TaskForgeException(exception.getMessage(), exception);
		}
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
		
		String email = (String) authResult.getPrincipal();
		
		Member authenticatedMember = userRepository.findByEmail(email)
				.orElseThrow(() -> new TaskForgeException("User not found with email: " + email));
		
		Set<String> roles = authenticatedMember.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
		String accessToken = jwtTokenProvider.generateAccessToken(email, roles);
		String refreshToken = jwtTokenProvider.generateRefreshToken(email, roles);
		
		Cookie accessCookie = new Cookie("access_token", accessToken);
		accessCookie.setHttpOnly(true);
		accessCookie.setPath("/");
		accessCookie.setMaxAge(60 * 60 * 24 * 3);
		accessCookie.setSecure(true);
		
		Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
		refreshCookie.setHttpOnly(true);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(60 * 60 * 24 * 7);
		refreshCookie.setSecure(true);
		
		response.addCookie(accessCookie);
		response.addCookie(refreshCookie);
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write("Authentication successful. Access and refresh tokens set in cookies.");
	}
	
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
		(new TaskForgeAuthenticationFailureHandler(objectMapper)).onAuthenticationFailure(request, response, failed);
	}
}
