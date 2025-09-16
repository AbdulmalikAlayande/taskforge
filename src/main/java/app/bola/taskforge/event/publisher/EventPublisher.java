package app.bola.taskforge.event.publisher;

import app.bola.taskforge.event.TaskForgeEvent;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class EventPublisher extends ApplicationEventPublisher {
    
    public void publishEvent(TaskForgeEvent event) {
        log.info("Publishing Event:: {}", event);
    }


}
