package app.bola.taskforge.event;

import lombok.Getter;
import lombok.Setter;
import java.lang.StringBuilder;

@Getter
@Setter
public class ProjectEvent extends TaskForgeEvent {

	private String projectId;
    private EventType eventType;
	private final String sourceEntityType = "project";
    
    public ProjectEvent(Object source) {
        super(source);
    }

    public enum EventType {

        PROJECT_CREATED,
		PROJECT_COMPLETED,
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Project [")
                                .append(sourceEntityType.toUpperCase()+", ")
                                .append(eventType)
                                .append("]");

    }
    
}
