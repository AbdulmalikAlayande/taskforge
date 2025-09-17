package app.bola.taskforge.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(request, ex.getMessage(), "NOT_FOUND", 404);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(InvalidRequestException ex, HttpServletRequest request) {
        return buildErrorResponse(request, ex.getMessage(), "BAD_REQUEST", 400);
    }

    @ExceptionHandler(TaskForgeException.class)
    public ResponseEntity<Map<String, Object>> handleTaskForge(TaskForgeException ex, HttpServletRequest request) {
        return buildErrorResponse(request, ex.getMessage(), "TASKFORGE_ERROR", 400);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthFailed(AuthenticationFailedException ex, HttpServletRequest request) {
        return buildErrorResponse(request, ex.getMessage(), "AUTHENTICATION_FAILED", 401);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return buildErrorResponse(request, ex.getMessage(), "UNAUTHORIZED", 401);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(request, "An unexpected error occurred. Please try again later.", "INTERNAL_SERVER_ERROR", 500);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpServletRequest request, String message, String error, int status) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("message", message);
        errorDetails.put("error", error);
        errorDetails.put("status", status);
        return ResponseEntity.status(status).body(errorDetails);
    }
}
