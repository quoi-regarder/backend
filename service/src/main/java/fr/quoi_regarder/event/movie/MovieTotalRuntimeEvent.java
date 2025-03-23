package fr.quoi_regarder.event.movie;

import fr.quoi_regarder.commons.enums.SseEventType;
import fr.quoi_regarder.event.UserEvent;

import java.util.Map;
import java.util.UUID;

public class MovieTotalRuntimeEvent extends UserEvent {
    private final Map<String, Long> totalRuntime;

    public MovieTotalRuntimeEvent(Object source, UUID userId, Map<String, Long> result) {
        super(source, userId, SseEventType.MOVIE_TOTAL_RUNTIME_UPDATE);
        this.totalRuntime = result;
    }

    @Override
    public Object getEventData() {
        return totalRuntime;
    }
}
