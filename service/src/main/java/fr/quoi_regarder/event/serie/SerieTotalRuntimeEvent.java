package fr.quoi_regarder.event.serie;

import fr.quoi_regarder.commons.enums.SseEventType;
import fr.quoi_regarder.event.UserEvent;

import java.util.Map;
import java.util.UUID;

public class SerieTotalRuntimeEvent extends UserEvent {
    private final Map<String, Long> totalRuntime;

    public SerieTotalRuntimeEvent(Object source, UUID userId, Map<String, Long> result) {
        super(source, userId, SseEventType.SERIE_TOTAL_RUNTIME_UPDATE);
        this.totalRuntime = result;
    }

    @Override
    public Object getEventData() {
        return totalRuntime;
    }
} 