package app.bola.taskforge.exception;

import java.io.Serial;

public class TaskForgeException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public TaskForgeException(String message) {
		super(message);
	}

	public TaskForgeException(String message, Throwable cause) {
		super(message, cause);
	}

	public TaskForgeException(Throwable cause) {
		super(cause);
	}

}
