package app.bola.taskforge.event.publisher;

import app.bola.taskforge.event.TaskForgeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishEvent(TaskForgeEvent event) {
        log.info("Publishing Event:: {}", event);
        applicationEventPublisher.publishEvent(event);
    }


}
