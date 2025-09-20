package app.bola.taskforge.event;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Getter
@Setter
public class ProjectEvent extends TaskForgeEvent {

	private String projectId;
    private EventType eventType;
	private final String sourceEntityType = "project";
    
    public ProjectEvent(Object source) {
        super(source);
    }

    public ProjectEvent(Object source, String projectId, String eventType){
        this(source);
        this.projectId = projectId;
        this.eventType = EventType.fromEvent(eventType);
    }
	
    @Getter
    public enum EventType {

        PROJECT_CREATED("create"),
        PROJECT_COMPLETED("complete");

        private final String event;

        EventType(String event) {
            this.event = event;
        }
	    
	    public static EventType fromEvent(String event) {
            for (EventType type : values()) {
                if (type.getEvent().equalsIgnoreCase(event)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown event: " + event);
        }
    }
	
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				       .append("projectId", projectId)
				       .append("sourceEntityType", sourceEntityType)
				       .toString();
	}
}
