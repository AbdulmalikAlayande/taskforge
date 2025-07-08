package app.bola.taskforge.security.config;

import app.bola.taskforge.security.filter.TaskForgeAuthenticationFilter;
import app.bola.taskforge.security.filter.TaskForgeAuthorizationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

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
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}
	
	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOrigins(List.of("http://localhost:3000", "https://taskforge.app", "https://www.taskforge.app"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		config.setAllowedHeaders(List.of("Access-Control-Allow-Headers", "Access-Control-Allow-Credentials", "X-Tenant-ID", "X-Refresh-Token", "Authorization", "Content-Type", "Accept", "X-Requested-With", "Requestor-Type"));
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, TaskForgeAuthenticationFilter authenticationFilter, TaskForgeAuthorizationFilter authorizationFilter) throws Exception {
		return http.cors(Customizer.withDefaults())
			        .csrf(AbstractHttpConfigurer::disable)
					.headers(headers -> headers
						.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
	                    .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true).maxAgeInSeconds(63072000))
					)
					.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/api/admin/create-new").permitAll()
						.requestMatchers("/api/organizations/create-new").hasRole("ORGANIZATION_ADMIN")
						.requestMatchers("/api/project/**", "/api/task/assign/**").hasAnyRole("PROJECT_MANAGER", "ORGANIZATION_ADMIN")
						.requestMatchers("/api/members/**", "/api/comment/**").authenticated()
					)
					.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint((request, response, _) -> {
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.setContentType("application/json");

							response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
							response.setHeader("Access-Control-Allow-Credentials", "true");
							response.setHeader("Vary", "Origin");
							
							Map<String, Object> errorDetails = new HashMap<>();
							errorDetails.put("message", "Unauthorized: Authentication is required to access this resource.");
							errorDetails.put("error", "UNAUTHORIZED");
							errorDetails.put("status", 401);
							errorDetails.put("path", request.getRequestURI());
							
							response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
						})
						.accessDeniedHandler((request, response, _) -> {
							response.setContentType("application/json");
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);

							response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
							response.setHeader("Access-Control-Allow-Credentials", "true");
							response.setHeader("Vary", "Origin");
							
							Map<String, Object> errorResponse = new HashMap<>();
							errorResponse.put("responseCode", "403");
							errorResponse.put("responseMessage", "Access Denied: You do not have the required permissions to access this resource.");
							errorResponse.put("status", false);
							
							response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
						})
					)
					.addFilterBefore(authorizationFilter, TaskForgeAuthenticationFilter.class)
					.addFilterAt(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
					.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
					.build();
	}

}
