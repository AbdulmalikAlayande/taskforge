package app.bola.taskforge.security.filter;

import app.bola.taskforge.domain.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class TenantFilter extends OncePerRequestFilter {
	
	private final List<String> REQUIRE_TENANT_UNAWARE_ENDPOINTS = List.of(
		"/api/organization/create-new", "/api/admin/create-new", "/swagger-ui",
		"/api/auth/oauth", "/api/auth/login", "/api/log/create-new",
		"/swagger-ui/index.html", "/swagger-ui.html", "/api-docs" ,
		"/swagger-ui/index.css", "/favicon.ico", "/api/health"
	);
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TenantFilter.class);
	
	private boolean isTenantUnAware(String uri) {
		return uri.startsWith("/api-docs") ||
				       uri.startsWith("/swagger-ui") ||
				       uri.startsWith("/webjars") ||
				       uri.startsWith("/swagger-resources") ||
				       REQUIRE_TENANT_UNAWARE_ENDPOINTS.contains(uri);
	}
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
	                                @NonNull HttpServletResponse response,
	                                @NonNull FilterChain filterChain) throws ServletException, IOException {

		String requestPath = request.getRequestURI();
		
		if (isTenantUnAware(requestPath)) {
			logger.info("Skipping TenantFilter for endpoint: {}", requestPath);
			filterChain.doFilter(request, response);
			return;
		}
		logger.info("Processing TenantFilter for endpoint: {}", requestPath);
		
		String tenantId = request.getHeader("X-Tenant-ID");

		if (tenantId != null && !tenantId.isEmpty()) {
			/*
			 * fixme: Here is a big black bug: 
			 *  so I am just going to set the current tenant ID based on what's coming from the frontend?
			 *  Nah!!! what if the tenant ID is not correct? so we just set it like that?
			 *  Das a stoopid bug
			 */
			TenantContext.setCurrentTenant(tenantId);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing X-Tenant-ID header");
			return;
		}

		try {
			logger.info("Current Tenant set to: {}", TenantContext.getCurrentTenant());
			filterChain.doFilter(request, response);
		} finally {
			TenantContext.clear();
		}

	}
}
