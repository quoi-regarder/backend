package fr.quoi_regarder.event.serie;

import fr.quoi_regarder.commons.enums.EventAction;
import fr.quoi_regarder.commons.enums.SerieContext;
import fr.quoi_regarder.commons.enums.WatchStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;
import java.util.UUID;

@Getter
public class SerieLoadBackgroundEvent extends ApplicationEvent {
    private final UUID userId;
    private final Long serieId;
    private final Long contextId;
    private final SerieContext context;
    private final EventAction action;
    private final WatchStatus watchStatus;
    private final Map<String, Object> serieDetails;
    private final String userLanguage;

    public SerieLoadBackgroundEvent(Object source, UUID userId, Long serieId, Long contextId, SerieContext context, EventAction action, WatchStatus watchStatus, String userLanguage, Map<String, Object> serieDetails) {
        super(source);
        this.userId = userId;
        this.serieId = serieId;
        this.contextId = contextId;
        this.context = context;
        this.action = action;
        this.watchStatus = watchStatus;
        this.serieDetails = serieDetails;
        this.userLanguage = userLanguage;
    }
} 