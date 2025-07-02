package app.bola.taskforge.exception;

public class UnauthorizedException extends TaskForgeException {
	
	
	public UnauthorizedException(Throwable cause) {
		super(cause);
	}
	
	public UnauthorizedException(String message) {
		super(message);
	}
	
	public UnauthorizedException(String message, Throwable cause) {
		super(message, cause);
	}
}
