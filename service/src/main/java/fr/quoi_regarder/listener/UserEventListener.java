package fr.quoi_regarder.listener;

import fr.quoi_regarder.event.UserEvent;
import fr.quoi_regarder.service.sse.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for user events and dispatches them to SSE clients.
 */
@Component
@RequiredArgsConstructor
public class UserEventListener {
    private final SseService sseService;

    @EventListener
    public void handleUserEvent(UserEvent event) {
        sseService.sendToUser(
                event.getUserId(),
                event.getEventType(),
                event.getEventData()
        );
    }
}