package app.bola.taskforge.exception;

import app.bola.taskforge.service.dto.OrganizationRequest;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

public class InvalidRequestException extends RuntimeException {
	
	public InvalidRequestException(Throwable throwable) {
		super(throwable);
	}
	
	public InvalidRequestException(String message) {
		super(message);
	}
	
	public InvalidRequestException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
