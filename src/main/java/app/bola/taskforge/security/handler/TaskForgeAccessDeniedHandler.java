package app.bola.taskforge.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class TaskForgeAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    public TaskForgeAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Vary", "Origin");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("responseCode", 403);
        errorResponse.put("responseMessage", "Access Denied: You do not have the required permissions to access this resource.");
        errorResponse.put("status", false);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
