package app.bola.taskforge.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProjectStatus {
	
	ACTIVE,
	PAUSED,
	COMPLETED,
	ABANDONED,
	ARCHIVED;
	
	/**
	 * Converts a string to a ProjectStatus enum.
	 *
	 * @param value the string representation of the project status
	 * @return the corresponding ProjectStatus enum
	 * @throws IllegalArgumentException if the string does not match any ProjectStatus
	 */
	@JsonCreator
	public static ProjectStatus fromString(String value) {
		
		for (ProjectStatus status : ProjectStatus.values()) {
			if (status.toString().equalsIgnoreCase(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Invalid ProjectStatus: " + value);
		
	}
}
