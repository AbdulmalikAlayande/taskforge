package app.bola.taskforge.exception;

public class EntityNotFoundException extends TaskForgeException{
	
	
	public EntityNotFoundException(Throwable cause) {
		super(cause);
	}
	
	public EntityNotFoundException(String message) {
		super(message);
	}
	
	public EntityNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
