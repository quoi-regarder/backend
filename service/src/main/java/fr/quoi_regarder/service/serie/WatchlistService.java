package fr.quoi_regarder.service.serie;

import fr.quoi_regarder.commons.enums.EventAction;
import fr.quoi_regarder.commons.enums.SerieContext;
import fr.quoi_regarder.commons.enums.WatchStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface WatchlistService<T> {
    /**
     * Handle watchlist action
     */
    @Transactional
    void handleAction(UUID userId, Long serieId, Long contextId, EventAction action, WatchStatus watchStatus);

    /**
     * Get the context this service handles
     */
    SerieContext getContext();
} 