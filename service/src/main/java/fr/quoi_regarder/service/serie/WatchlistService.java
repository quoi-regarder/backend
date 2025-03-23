package fr.quoi_regarder.service.serie;

import fr.quoi_regarder.commons.enums.SerieContext;
import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.event.serie.SerieDataLoadedEvent;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

public interface WatchlistService<T, E extends SerieDataLoadedEvent> {
    /**
     * Add an item to watchlist
     */
    @Transactional
    void addToWatchlist(UUID userId, Long serieId, T dto);

    /**
     * Update item status in watchlist
     */
    @Transactional
    void updateStatus(UUID userId, Long serieId, Long contextId, WatchStatus status);

    /**
     * Remove item from watchlist
     */
    @Transactional
    void removeFromWatchlist(UUID userId, Long serieId, Long contextId);

    /**
     * Handle data loaded event
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void handleDataLoadedEvent(E event);

    /**
     * Get the context this service handles
     */
    SerieContext getContext();
} 