package fr.quoi_regarder.event.movie;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Événement déclenché lorsqu'une watchlist utilisateur est modifiée
 */
@Getter
public class MovieWatchlistChangedEvent extends ApplicationEvent {
    private final UUID userId;

    public MovieWatchlistChangedEvent(Object source, UUID userId) {
        super(source);
        this.userId = userId;
    }
} 