package fr.quoi_regarder.event;

import fr.quoi_regarder.commons.enums.SseEventType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Base event class for user-specific notifications.
 */
@Getter
public abstract class UserEvent extends ApplicationEvent {
    private final UUID userId;
    private final SseEventType eventType;

    public UserEvent(Object source, UUID userId, SseEventType eventType) {
        super(source);
        this.userId = userId;
        this.eventType = eventType;
    }

    /**
     * Get the data to be sent with this event.
     *
     * @return the event data
     */
    public abstract Object getEventData();
}