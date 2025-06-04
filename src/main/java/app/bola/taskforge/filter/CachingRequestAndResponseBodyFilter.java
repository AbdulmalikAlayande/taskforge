package app.bola.taskforge.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@Order(1) // I want this filter to run before other filters that might need to cache request/response bodies
public class CachingRequestAndResponseBodyFilter implements Filter {
	
	
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		
		ContentCachingRequestWrapper cachedRequest = new ContentCachingRequestWrapper((HttpServletRequest) servletRequest);
		ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper((HttpServletResponse) servletResponse);
		
		filterChain.doFilter(cachedRequest, cachedResponse);
		cachedResponse.copyBodyToResponse();
	}
}
