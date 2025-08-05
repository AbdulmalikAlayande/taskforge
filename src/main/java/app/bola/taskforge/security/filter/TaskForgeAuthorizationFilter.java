package app.bola.taskforge.security.filter;

import app.bola.taskforge.security.provider.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskForgeAuthorizationFilter extends OncePerRequestFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(TaskForgeAuthorizationFilter.class);
	public static final String BEARER_ = "Bearer ";
	private final JwtTokenProvider jwtTokenProvider;
	private final UserDetailsService userDetailsService;
	public final List<String> UNPROTECTED_PATHS = List.of(
			"/api/auth/login",
			"/api/auth/oauth",
			"/api/admin/create-new",
			"/api/health",
			"/swagger-ui/**",
			"/v3/api-docs/**"
	);
	
	
	
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
	                                @NonNull FilterChain filterChain) throws ServletException, IOException {
		
		if (UNPROTECTED_PATHS.contains(request.getRequestURI())) {
			filterChain.doFilter(request, response);
			return;
		}
		
		String token;
		boolean authenticationSuccessful = false;
		
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith(BEARER_)) {

			token = authHeader.substring(BEARER_.length());
			if (StringUtils.isNotBlank(token)) {
				try {
					authenticationSuccessful = authorizeToken(token, request);
				} catch (Exception exception) {
					logger.debug("Bearer token authentication failed: {}", exception.getMessage());
				}
			}
		}
		
		if (!authenticationSuccessful && request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("access_token".equals(cookie.getName())) {
					token = cookie.getValue();
					if (StringUtils.isNotBlank(token)) {
						try {
							authenticationSuccessful = authorizeToken(token, request);
						} catch (Exception exception) {
							logger.debug("Cookie token authentication failed: {}", exception.getMessage());
						}
					}
					break;
				}
			}
		}
		
		if (!authenticationSuccessful) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
			return;
		}
		
		filterChain.doFilter(request, response);
	}
	
	private boolean authorizeToken(String token, HttpServletRequest request) {
		try {
			if (jwtTokenProvider.isValidToken(token) && !jwtTokenProvider.isExpiredToken(token)) {
				String email = (String) jwtTokenProvider.extractClaimFromToken(token, "email");
				UserDetails user = userDetailsService.loadUserByUsername(email);
				UsernamePasswordAuthenticationToken authToken =
						new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
				return true;
			}
		} catch (Exception exception) {
			logger.error("Token authorization failed: {}", exception.getMessage());
		}
		return false;
	}
}
