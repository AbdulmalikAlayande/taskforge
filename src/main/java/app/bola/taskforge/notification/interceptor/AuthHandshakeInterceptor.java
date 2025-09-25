package app.bola.taskforge.notification.interceptor;

import app.bola.taskforge.security.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {
	
	final JwtTokenProvider tokenProvider;
	
	@Override
	public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
	                               @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
		HttpHeaders headers = request.getHeaders();
		String authHeader = headers.getFirst("Authorization");
		String tenantId = headers.getFirst("X-Tenant-ID");
		
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
			return false;
		}
		
		if (tenantId == null || tenantId.isEmpty()) {
			response.setStatusCode(org.springframework.http.HttpStatus.BAD_REQUEST);
			return false;
		}
		
		String token = authHeader.substring(7);
		if (!tokenProvider.isValidToken(token) || !tokenProvider.isExpiredToken(token)) {
			response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
			return false;
		}
		
		String userId = tokenProvider.extractClaimFromToken(token, "userId").toString();
		attributes.put("userId", userId);
		attributes.put("tenantId", tenantId);
		
		return true;
	}
	
	@Override
	public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
	                           @NonNull WebSocketHandler wsHandler, Exception exception) {
		// No-op
	}
}
