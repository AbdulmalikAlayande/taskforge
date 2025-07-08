package app.bola.taskforge.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
	
	private final ObjectMapper objectMapper;
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer
						.ignoringRequestMatchers("/api/log/create-new", "/api/auth/**")
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
					)
					.headers(headers -> headers
						.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
	                    .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true).maxAgeInSeconds(63072000))
					)
					.cors(cors -> cors.configurationSource(_ -> {
						var corsConfig = new CorsConfiguration();
						corsConfig.setAllowedOrigins(List.of("http://localhost:3000", "https://taskforge.app", "https://www.taskforge.app"));
						corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
						corsConfig.setAllowedHeaders(List.of("X-Tenant-ID", "X-Refresh-Token", "Authorization", "Content-Type", "Accept", "X-Requested-With", "Requestor-Type"));
						corsConfig.setAllowCredentials(true);
						return corsConfig;
					}))
					.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers("/api/admin/**", "api/organizations/create-new").hasRole("ORGANIZATION_ADMIN")
						.requestMatchers("api/project/**", "api/task/assign/**").hasAnyRole("PROJECT_MANAGER", "ORGANIZATION_ADMIN")
						.requestMatchers("api/members/**", "api/comment/**").authenticated()
					)
					.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint((request, response, _) -> {
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.setContentType("application/json");
							
							Map<String, Object> errorDetails = new HashMap<>();
							errorDetails.put("message", "Unauthorized: Authentication is required to access this resource.");
							errorDetails.put("error", "UNAUTHORIZED");
							errorDetails.put("status", 401);
							errorDetails.put("path", request.getRequestURI());
							
							response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
						})
						.accessDeniedHandler((_, response, _) -> {
							response.setContentType("application/json");
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							
							Map<String, Object> errorResponse = new HashMap<>();
							errorResponse.put("responseCode", "403");
							errorResponse.put("responseMessage", "Access Denied: You do not have the required permissions to access this resource.");
							errorResponse.put("status", false);
							
							response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
						})
					)
					.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
					.build();
	}
	
}
