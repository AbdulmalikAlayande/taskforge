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
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TenantFilter.class);
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
	                                @NonNull HttpServletResponse response,
	                                @NonNull FilterChain filterChain) throws ServletException, IOException {

		String requestPath = request.getRequestURI();
		
		List<String> requireTenantUnawareEndpoints = List.of(
			"/api/organization/create-new", "/api/admin/create-new",
			"/api/auth/oauth", "/api/auth/login", "/api/log/create-new",
			"/swagger-ui.html", "/swagger-ui/**", "/api-docs/**"
		);
		
		if (requireTenantUnawareEndpoints.contains(requestPath)) {
			filterChain.doFilter(request, response);
			return;
		}
		
		
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
