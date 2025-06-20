package app.bola.taskforge.filter;

import app.bola.taskforge.domain.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Order(1) // I want to make sure that this filter runs before any other filters that depend on the tenant context
@Component
public class TenantFilter extends OncePerRequestFilter {


	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
	                                @NonNull HttpServletResponse response,
	                                @NonNull FilterChain filterChain) throws ServletException, IOException {

		String requestPath = request.getRequestURI();

		// Skip tenant validation for organization creation endpoint
		if ("/api/organizations/create-new".equals(requestPath)) {
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
