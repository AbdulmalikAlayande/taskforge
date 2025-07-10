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
	
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
	                                @NonNull HttpServletResponse response,
	                                @NonNull FilterChain filterChain) throws ServletException, IOException {

		String requestPath = request.getRequestURI();
		
		List<String> requireTenantUnawareEndpoints = List.of("/api/organization/create-new", "/api/admin/create-new",
				"/api/auth/oauth", "/api/auth/login", "/api/log/create-new");
		
		if (requireTenantUnawareEndpoints.contains(requestPath)) {
			filterChain.doFilter(request, response);
			return;
		}

		String tenantId = request.getHeader("X-Tenant-ID");

		if (tenantId != null && !tenantId.isEmpty()) {
			TenantContext.setCurrentTenant(tenantId);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing X-Tenant-ID header");
			return;
		}

		try {
			filterChain.doFilter(request, response);
		} finally {
			TenantContext.clear();
		}

	}
}
