package app.bola.taskforge.interceptor;

import app.bola.taskforge.domain.entity.AuditLog;
import app.bola.taskforge.domain.enums.AuditLogEvent;
import app.bola.taskforge.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class AuditLogInterceptor implements HandlerInterceptor {
	
	private final AuditLogRepository auditLogRepository;
	
	@Override
	public boolean preHandle(@NonNull HttpServletRequest request,
	                         @NonNull HttpServletResponse response,
	                         @NonNull Object handler) {
		
		AuditLog auditLog = AuditLog.builder()
				                    .timestamp(LocalDateTime.now())
				                    .eventType(AuditLogEvent.REQUEST_RECEIVED)
				                    .endpointUri(request.getRequestURI())
				                    .httpMethod(request.getMethod())
				                    .requestIpAddress(request.getRemoteAddr())
				                    .userId(request.getRemoteUser() != null ? request.getRemoteUser() : "anonymous")
				                    .requestPayload(getRequestBody(request))
				                    .userAgent(request.getHeader("User-Agent"))
				                    .build();
		
		request.setAttribute("auditLog", auditLog);
		return true;
	}
	
	private String getRequestBody(HttpServletRequest request) {
		ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request);
		byte[] buffer = wrapper.getContentAsByteArray();
		
		if (buffer.length > 0) {
			return new String(buffer, StandardCharsets.UTF_8);
		}
		return "";
	}
	
	@Override
	public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
	                       @NonNull Object handler, ModelAndView modelAndView) throws Exception {
		
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}
	
	@Override
	public void afterCompletion(@NonNull HttpServletRequest request,
	                            @NonNull HttpServletResponse response,
	                            @NonNull Object handler, Exception ex) {
		
		AuditLog auditLog = (AuditLog) request.getAttribute("auditLog");
		if (auditLog != null) {
			auditLog.setResponsePayload(getResponsePayLoad(response));
			auditLog.setStatusCode(response.getStatus());
			auditLogRepository.save(auditLog);
		}
	}
	
	private String getResponsePayLoad(HttpServletResponse response) {
		ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
		byte[] buffer = wrapper.getContentAsByteArray();
		
		if (buffer.length > 0) {
			return new String(buffer, StandardCharsets.UTF_8);
		}
		return "";
	}
}
