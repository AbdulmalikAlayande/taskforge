package app.bola.taskforge.notification.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebsocketLoggingHandshakeInterceptor implements HandshakeInterceptor {
	
	
	@Override
	public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
	                               @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
		return false;
	}
	
	@Override
	public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
	                           @NonNull WebSocketHandler wsHandler, Exception exception) {
		
	}
}
